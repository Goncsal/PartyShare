package tqs.backend.tqsbackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tqs.backend.tqsbackend.dto.BookingCreateRequest;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.service.BookingService;
import tqs.backend.tqsbackend.service.UserService;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private tqs.backend.tqsbackend.service.ItemService itemService;

    @MockitoBean
    private UserService userService;

    @Test
    void getBookingRequests_Owner_ReturnsView() throws Exception {
        User owner = new User();
        owner.setRole(UserRoles.OWNER);
        given(userService.getUserById(1L)).willReturn(Optional.of(owner));
        given(bookingService.getPendingBookingsByOwner(1L)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/bookings/requests")
                .sessionAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("bookings/requests"))
                .andExpect(model().attributeExists("bookings"));
    }

    @Test
    void getBookingRequests_NotOwner_Redirects() throws Exception {
        User renter = new User();
        renter.setRole(UserRoles.RENTER);
        given(userService.getUserById(1L)).willReturn(Optional.of(renter));

        mockMvc.perform(get("/bookings/requests")
                .sessionAttr("userId", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/search"));
    }

    @Test
    void createBooking_WithOffer_Success() throws Exception {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setItemId(1L);
        request.setProposedPrice(15.0);

        Booking booking = new Booking();
        booking.setId(1L);

        given(bookingService.createBooking(any(BookingCreateRequest.class))).willReturn(booking);

        mockMvc.perform(post("/bookings")
                .sessionAttr("userId", 1L)
                .param("itemId", "1")
                .param("startDate", "2026-01-01")
                .param("endDate", "2026-01-03")
                .param("proposedPrice", "15.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"));

        verify(bookingService)
                .createBooking(argThat(req -> req.getProposedPrice().equals(15.0) && req.getItemId().equals(1L)));
    }

    @Test
    void acceptBooking_Owner_Success() throws Exception {
        User owner = new User();
        owner.setRole(UserRoles.OWNER);
        given(userService.getUserById(1L)).willReturn(Optional.of(owner));

        Booking booking = new Booking();
        booking.setId(1L);
        given(bookingService.acceptBooking(1L, 1L)).willReturn(booking);

        mockMvc.perform(post("/bookings/1/accept")
                .sessionAttr("userId", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/requests"));

        verify(bookingService).acceptBooking(1L, 1L);
    }

    @Test
    void declineBooking_Owner_Success() throws Exception {
        User owner = new User();
        owner.setRole(UserRoles.OWNER);
        given(userService.getUserById(1L)).willReturn(Optional.of(owner));

        Booking booking = new Booking();
        booking.setId(1L);
        given(bookingService.declineBooking(1L, 1L)).willReturn(booking);

        mockMvc.perform(post("/bookings/1/decline")
                .sessionAttr("userId", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/requests"));

        verify(bookingService).declineBooking(1L, 1L);
    }

    @Test
    void counterOfferBooking_Owner_Success() throws Exception {
        User owner = new User();
        owner.setRole(UserRoles.OWNER);
        given(userService.getUserById(1L)).willReturn(Optional.of(owner));

        Booking booking = new Booking();
        booking.setId(1L);
        given(bookingService.counterOfferBooking(1L, 40.0, 1L)).willReturn(booking);

        mockMvc.perform(post("/bookings/1/counter-offer")
                .sessionAttr("userId", 1L)
                .param("price", "40.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings/requests"));

        verify(bookingService).counterOfferBooking(1L, 40.0, 1L);
    }

    @Test
    void acceptCounterOffer_Renter_Success() throws Exception {
        User renter = new User();
        renter.setRole(UserRoles.RENTER);
        given(userService.getUserById(1L)).willReturn(Optional.of(renter));

        Booking booking = new Booking();
        booking.setId(1L);
        given(bookingService.acceptCounterOffer(1L, 1L)).willReturn(booking);

        mockMvc.perform(post("/bookings/1/accept-counter-offer")
                .sessionAttr("userId", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"));

        verify(bookingService).acceptCounterOffer(1L, 1L);
    }

    @Test
    void declineCounterOffer_Renter_Success() throws Exception {
        User renter = new User();
        renter.setRole(UserRoles.RENTER);
        given(userService.getUserById(1L)).willReturn(Optional.of(renter));

        Booking booking = new Booking();
        booking.setId(1L);
        given(bookingService.declineCounterOffer(1L, 1L)).willReturn(booking);

        mockMvc.perform(post("/bookings/1/decline-counter-offer")
                .sessionAttr("userId", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bookings"));

        verify(bookingService).declineCounterOffer(1L, 1L);
    }
}
