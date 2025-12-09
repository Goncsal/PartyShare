package tqs.backend.tqsbackend.controller;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.repository.ItemRepository;
import tqs.backend.tqsbackend.service.PaymentResult;
import tqs.backend.tqsbackend.service.PaymentService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("dev")
class BookingControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ItemRepository itemRepository;

        @Autowired
        private PaymentService paymentService;

        @TestConfiguration
        static class TestConfig {
                @Bean
                public PaymentService paymentService() {
                        return Mockito.mock(PaymentService.class);
                }
        }

        @Test
        void shouldRenderRentForm() throws Exception {
                Item item = itemRepository.findAll().get(0);

                mockMvc.perform(get("/bookings/rent/" + item.getId())
                                .sessionAttr("userId", 1L))
                                .andExpect(status().isOk())
                                .andExpect(view().name("bookings/rent_item"))
                                .andExpect(model().attributeExists("bookingRequest"))
                                .andExpect(model().attribute("item", hasProperty("id", is(item.getId()))));
        }

        @Test
    void shouldCreateBookingFromForm() throws Exception {
        Item item = itemRepository.findAll().get(0);

        when(paymentService.charge(anyLong(), any(Item.class), any(), anyLong()))
                .thenReturn(PaymentResult.success("ref-ui-1"));

        mockMvc.perform(post("/bookings")
                .sessionAttr("userId", 1L)
                .param("itemId", item.getId().toString())
                .param("renterId", "999")
                .param("startDate", LocalDate.now().plusDays(1).toString())
                .param("endDate", LocalDate.now().plusDays(3).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/payment/*"));
    }
}
