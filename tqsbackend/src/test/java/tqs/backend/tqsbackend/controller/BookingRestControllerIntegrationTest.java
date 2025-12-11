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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
class BookingRestControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private BookingRepository bookingRepository;

  @MockitoBean
  private PaymentService paymentService;

  @Test
  void shouldCreateBookingViaApi() throws Exception {
    Item item = itemRepository.findAll().get(0);
    String start = LocalDate.now().plusDays(1).toString();
    String end = LocalDate.now().plusDays(3).toString();

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
        .andExpect(jsonPath("$.status", is("REQUESTED")))
        .andExpect(jsonPath("$.paymentStatus", is("PENDING")))
        .andExpect(jsonPath("$.totalPrice", notNullValue()));
  }

  @Test
  void shouldReturnConflictWhenOverlap() throws Exception {
    Item item = itemRepository.findAll().get(0);
    LocalDate start = LocalDate.now().plusDays(5);
    LocalDate end = LocalDate.now().plusDays(7);

    Booking existing = new Booking(item, 10L, start, end, BigDecimal.valueOf(item.getPrice()),
        BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(2)), BookingStatus.ACCEPTED,
        PaymentStatus.PAID);
    bookingRepository.save(existing);

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
