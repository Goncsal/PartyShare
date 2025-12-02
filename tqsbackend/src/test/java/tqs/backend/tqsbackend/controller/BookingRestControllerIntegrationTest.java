package tqs.backend.tqsbackend.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.PaymentStatus;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.service.PaymentResult;
import tqs.backend.tqsbackend.service.PaymentService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
class BookingRestControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private BookingRepository bookingRepository;

  @MockBean
  private PaymentService paymentService;

  @Test
  void shouldCreateBookingViaApi() throws Exception {
    Item item = itemRepository.findAll().get(0);
    String start = LocalDate.now().plusDays(1).toString();
    String end = LocalDate.now().plusDays(3).toString();

    when(paymentService.charge(anyLong(), any(Item.class), any(), anyLong()))
        .thenReturn(PaymentResult.success("ref-int-1"));

    mockMvc.perform(post("/api/bookings")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "itemId": %d,
              "renterId": 999,
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(item.getId(), start, end)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status", is("CONFIRMED")))
        .andExpect(jsonPath("$.paymentStatus", is("PAID")))
        .andExpect(jsonPath("$.paymentReference", is("ref-int-1")))
        .andExpect(jsonPath("$.totalPrice", notNullValue()));
  }

  @Test
  void shouldReturnConflictWhenOverlap() throws Exception {
    Item item = itemRepository.findAll().get(0);
    LocalDate start = LocalDate.now().plusDays(5);
    LocalDate end = LocalDate.now().plusDays(7);

    Booking existing = new Booking(item, 10L, start, end, BigDecimal.valueOf(item.getPrice()),
        BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(2)), BookingStatus.CONFIRMED,
        PaymentStatus.PAID);
    bookingRepository.save(existing);

    when(paymentService.charge(anyLong(), any(Item.class), any(), anyLong()))
        .thenReturn(PaymentResult.success("ref-int-2"));

    mockMvc.perform(post("/api/bookings")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "itemId": %d,
              "renterId": 999,
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(item.getId(), start.plusDays(1), end.plusDays(1))))
        .andExpect(status().isConflict());
  }

  @Test
  void shouldReturnPaymentRequiredWhenPaymentFails() throws Exception {
    Item item = itemRepository.findAll().get(0);
    String start = LocalDate.now().plusDays(2).toString();
    String end = LocalDate.now().plusDays(4).toString();

    when(paymentService.charge(anyLong(), any(Item.class), any(), anyLong()))
        .thenReturn(PaymentResult.failure("gateway-error"));

    mockMvc.perform(post("/api/bookings")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "itemId": %d,
              "renterId": 999,
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(item.getId(), start, end)))
        .andExpect(status().isPaymentRequired());

    List<Booking> bookings = bookingRepository.findAll();
    assertThat(bookings).isNotEmpty();
    assertThat(bookings.get(0).getStatus()).isEqualTo(BookingStatus.REJECTED);
  }

  @Test
  void shouldValidateDates() throws Exception {
    Item item = itemRepository.findAll().get(0);
    String start = LocalDate.now().minusDays(1).toString();
    String end = LocalDate.now().plusDays(1).toString();

    mockMvc.perform(post("/api/bookings")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "itemId": %d,
              "renterId": 999,
              "startDate": "%s",
              "endDate": "%s"
            }
            """.formatted(item.getId(), start, end)))
        .andExpect(status().isBadRequest());
  }
}
