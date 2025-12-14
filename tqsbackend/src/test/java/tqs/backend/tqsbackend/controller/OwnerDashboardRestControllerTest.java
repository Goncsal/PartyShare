package tqs.backend.tqsbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.entity.*;
import tqs.backend.tqsbackend.repository.BookingRepository;
import tqs.backend.tqsbackend.service.ItemService;
import tqs.backend.tqsbackend.service.RatingService;
import tqs.backend.tqsbackend.service.ReportService;
import tqs.backend.tqsbackend.service.UserService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerDashboardRestController.class)
@Import(OwnerDashboardRestControllerTest.TestConfig.class)
public class OwnerDashboardRestControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private ItemService itemService;

        @MockitoBean
        private RatingService ratingService;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private ReportService reportService;

        @MockitoBean
        private BookingRepository bookingRepository;

        private MockHttpSession session;
        private User ownerUser;
        private User renterUser;
        private Category category;
        private Item item1;
        private Item item2;

        @TestConfiguration
        static class TestConfig {
                @Bean
                public ObjectMapper objectMapper() {
                        return new ObjectMapper();
                }
        }

        @BeforeEach
        void setUp() {
                session = new MockHttpSession();

                category = new Category("Electronics");
                category.setId(1L);

                ownerUser = new User("Owner User", "owner@example.com", "password", UserRoles.OWNER);
                ownerUser.setId(1L);

                renterUser = new User("Renter User", "renter@example.com", "password", UserRoles.RENTER);
                renterUser.setId(2L);

                item1 = new Item("Item 1", "Description 1", 10.0, category, 4.5, "Location 1", 1L);
                item1.setId(1L);
                item1.setActive(true);

                item2 = new Item("Item 2", "Description 2", 20.0, category, 3.5, "Location 2", 1L);
                item2.setId(2L);
                item2.setActive(false);
        }

        @Test
        void testGetOwnerItems_Success() throws Exception {
                session.setAttribute("userId", 1L);
                List<Item> items = Arrays.asList(item1, item2);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(itemService.findByOwnerId(1L)).willReturn(items);

                mockMvc.perform(get("/api/owner/dashboard/items")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Item 1"))
                                .andExpect(jsonPath("$[1].name").value("Item 2"));

                verify(itemService, times(1)).findByOwnerId(1L);
        }

        @Test
        void testGetOwnerItems_NotLoggedIn() throws Exception {
                mockMvc.perform(get("/api/owner/dashboard/items"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("User not logged in"));

                verify(itemService, never()).findByOwnerId(anyLong());
        }

        @Test
        void testGetOwnerItems_NotOwner() throws Exception {
                session.setAttribute("userId", 2L);
                given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

                mockMvc.perform(get("/api/owner/dashboard/items")
                                .session(session))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("User is not an owner"));

                verify(itemService, never()).findByOwnerId(anyLong());
        }

        @Test
        void testActivateItem_Success() throws Exception {
                session.setAttribute("userId", 1L);
                item2.setActive(true);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(itemService.activateItem(2L, 1L)).willReturn(item2);

                mockMvc.perform(patch("/api/owner/dashboard/items/2/activate")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.active").value(true));

                verify(itemService, times(1)).activateItem(2L, 1L);
        }

        @Test
        void testActivateItem_NotOwnerOfItem() throws Exception {
                session.setAttribute("userId", 1L);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(itemService.activateItem(2L, 1L))
                                .willThrow(new IllegalArgumentException("User 1 is not the owner of item 2"));

                mockMvc.perform(patch("/api/owner/dashboard/items/2/activate")
                                .session(session))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("User 1 is not the owner of item 2"));
        }

        @Test
        void testDeactivateItem_Success() throws Exception {
                session.setAttribute("userId", 1L);
                item1.setActive(false);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(itemService.deactivateItem(1L, 1L)).willReturn(item1);

                mockMvc.perform(patch("/api/owner/dashboard/items/1/deactivate")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.active").value(false));

                verify(itemService, times(1)).deactivateItem(1L, 1L);
        }

        @Test
        void testGetItemRatings_Success() throws Exception {
                session.setAttribute("userId", 1L);
                Rating rating1 = new Rating(2L, RatingType.PRODUCT, 1L, 5, "Great item!");
                rating1.setId(1L);
                Rating rating2 = new Rating(3L, RatingType.PRODUCT, 1L, 4, "Good item");
                rating2.setId(2L);
                List<Rating> ratings = Arrays.asList(rating1, rating2);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(itemService.getItemById(1L)).willReturn(item1);
                given(ratingService.getRatingByRatedInfo(RatingType.PRODUCT, 1L)).willReturn(ratings);

                mockMvc.perform(get("/api/owner/dashboard/items/1/ratings")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].rate").value(5))
                                .andExpect(jsonPath("$[0].comment").value("Great item!"))
                                .andExpect(jsonPath("$[1].rate").value(4));

                verify(ratingService, times(1)).getRatingByRatedInfo(RatingType.PRODUCT, 1L);
        }

        @Test
        void testGetItemRatings_NotOwnerOfItem() throws Exception {
                session.setAttribute("userId", 2L);
                item1.setOwnerId(1L);

                given(userService.getUserById(2L)).willReturn(Optional.of(ownerUser));
                given(itemService.getItemById(1L)).willReturn(item1);

                mockMvc.perform(get("/api/owner/dashboard/items/1/ratings")
                                .session(session))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("Item does not belong to this owner"));

                verify(ratingService, never()).getRatingByRatedInfo(any(), anyLong());
        }

        @Test
        void testActivateItem_NotLoggedIn() throws Exception {
                mockMvc.perform(patch("/api/owner/dashboard/items/1/activate"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("User not logged in"));

                verify(itemService, never()).activateItem(anyLong(), anyLong());
        }

        @Test
        void testActivateItem_NotOwnerRole() throws Exception {
                session.setAttribute("userId", 2L);
                given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

                mockMvc.perform(patch("/api/owner/dashboard/items/1/activate")
                                .session(session))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("User is not an owner"));

                verify(itemService, never()).activateItem(anyLong(), anyLong());
        }

        @Test
        void testDeactivateItem_NotLoggedIn() throws Exception {
                mockMvc.perform(patch("/api/owner/dashboard/items/1/deactivate"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("User not logged in"));

                verify(itemService, never()).deactivateItem(anyLong(), anyLong());
        }

        @Test
        void testDeactivateItem_NotOwnerRole() throws Exception {
                session.setAttribute("userId", 2L);
                given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

                mockMvc.perform(patch("/api/owner/dashboard/items/1/deactivate")
                                .session(session))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("User is not an owner"));

                verify(itemService, never()).deactivateItem(anyLong(), anyLong());
        }

        @Test
        void testDeactivateItem_NotOwnerOfItem() throws Exception {
                session.setAttribute("userId", 1L);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(itemService.deactivateItem(1L, 1L))
                                .willThrow(new IllegalArgumentException("User 1 is not the owner of item 1"));

                mockMvc.perform(patch("/api/owner/dashboard/items/1/deactivate")
                                .session(session))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("User 1 is not the owner of item 1"));
        }

        @Test
        void testGetItemRatings_NotLoggedIn() throws Exception {
                mockMvc.perform(get("/api/owner/dashboard/items/1/ratings"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("User not logged in"));

                verify(ratingService, never()).getRatingByRatedInfo(any(), anyLong());
        }

        @Test
        void testGetItemRatings_NotOwnerRole() throws Exception {
                session.setAttribute("userId", 2L);
                given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

                mockMvc.perform(get("/api/owner/dashboard/items/1/ratings")
                                .session(session))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("User is not an owner"));

                verify(ratingService, never()).getRatingByRatedInfo(any(), anyLong());
        }

        @Test
        void testGetItemRatings_ItemNotFound() throws Exception {
                session.setAttribute("userId", 1L);
                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(itemService.getItemById(99L)).willReturn(null);

                mockMvc.perform(get("/api/owner/dashboard/items/99/ratings")
                                .session(session))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Item not found"));

                verify(ratingService, never()).getRatingByRatedInfo(any(), anyLong());
        }

        // ========== DELETE ITEM TESTS ==========

        @Test
        void testDeleteItem_Success() throws Exception {
                session.setAttribute("userId", 1L);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                doNothing().when(itemService).deleteItem(1L, 1L);

                mockMvc.perform(delete("/api/owner/dashboard/items/1")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Item deleted successfully"));

                verify(itemService, times(1)).deleteItem(1L, 1L);
        }

        @Test
        void testDeleteItem_NotLoggedIn() throws Exception {
                mockMvc.perform(delete("/api/owner/dashboard/items/1"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("User not logged in"));

                verify(itemService, never()).deleteItem(anyLong(), anyLong());
        }

        @Test
        void testDeleteItem_NotOwnerRole() throws Exception {
                session.setAttribute("userId", 2L);
                given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

                mockMvc.perform(delete("/api/owner/dashboard/items/1")
                                .session(session))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("User is not an owner"));

                verify(itemService, never()).deleteItem(anyLong(), anyLong());
        }

        @Test
        void testDeleteItem_NotOwnerOfItem() throws Exception {
                session.setAttribute("userId", 1L);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                doThrow(new IllegalArgumentException("User 1 is not the owner of item 99"))
                                .when(itemService).deleteItem(99L, 1L);

                mockMvc.perform(delete("/api/owner/dashboard/items/99")
                                .session(session))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("User 1 is not the owner of item 99"));
        }

        // ========== CREATE DAMAGE REPORT TESTS ==========

        @Test
        void testCreateDamageReport_Success() throws Exception {
                session.setAttribute("userId", 1L);

                Booking pastBooking = new Booking();
                pastBooking.setId(1L);
                pastBooking.setRenterId(2L);
                pastBooking.setEndDate(LocalDate.now().minusDays(1));
                pastBooking.setItem(item1);

                Report report = new Report(1L, "Damage Report - Booking #1 - Item 1", "The tent had a large tear");
                report.setId(1L);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(pastBooking));
                given(reportService.createReport(eq(1L), anyString(), anyString())).willReturn(report);

                String requestBody = "{\"damageDescription\":\"The tent had a large tear\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/damage-report")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("Damage report created successfully"))
                                .andExpect(jsonPath("$.reportId").value(1));

                verify(reportService, times(1)).createReport(eq(1L), anyString(), anyString());
        }

        @Test
        void testCreateDamageReport_NotLoggedIn() throws Exception {
                String requestBody = "{\"damageDescription\":\"Damage description\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/damage-report")
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("User not logged in"));

                verify(reportService, never()).createReport(anyLong(), anyString(), anyString());
        }

        @Test
        void testCreateDamageReport_NotOwnerRole() throws Exception {
                session.setAttribute("userId", 2L);
                given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

                String requestBody = "{\"damageDescription\":\"Damage description\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/damage-report")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("User is not an owner"));

                verify(reportService, never()).createReport(anyLong(), anyString(), anyString());
        }

        @Test
        void testCreateDamageReport_BookingNotFound() throws Exception {
                session.setAttribute("userId", 1L);
                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(99L)).willReturn(Optional.empty());

                String requestBody = "{\"damageDescription\":\"Damage description\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/99/damage-report")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Booking not found"));

                verify(reportService, never()).createReport(anyLong(), anyString(), anyString());
        }

        @Test
        void testCreateDamageReport_NotOwnerOfBooking() throws Exception {
                session.setAttribute("userId", 1L);

                Booking booking = new Booking();
                booking.setId(1L);
                booking.setRenterId(2L);
                booking.setEndDate(LocalDate.now().minusDays(1));
                Item otherOwnerItem = new Item("Other Item", "Description", 10.0, category, 4.5, "Location", 99L);
                booking.setItem(otherOwnerItem);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(booking));

                String requestBody = "{\"damageDescription\":\"Damage description\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/damage-report")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("Booking does not belong to this owner"));

                verify(reportService, never()).createReport(anyLong(), anyString(), anyString());
        }

        @Test
        void testCreateDamageReport_FutureRental() throws Exception {
                session.setAttribute("userId", 1L);

                Booking futureBooking = new Booking();
                futureBooking.setId(1L);
                futureBooking.setRenterId(2L);
                futureBooking.setEndDate(LocalDate.now().plusDays(5));
                futureBooking.setItem(item1);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(futureBooking));

                String requestBody = "{\"damageDescription\":\"Damage description\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/damage-report")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error")
                                                .value("Cannot report damage for ongoing or future rentals"));

                verify(reportService, never()).createReport(anyLong(), anyString(), anyString());
        }

        @Test
        void testCreateDamageReport_EmptyDescription() throws Exception {
                session.setAttribute("userId", 1L);

                Booking pastBooking = new Booking();
                pastBooking.setId(1L);
                pastBooking.setRenterId(2L);
                pastBooking.setEndDate(LocalDate.now().minusDays(1));
                pastBooking.setItem(item1);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(pastBooking));

                String requestBody = "{\"damageDescription\":\"\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/damage-report")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Damage description is required"));

                verify(reportService, never()).createReport(anyLong(), anyString(), anyString());
        }

        // ========== GET RENTER RATING TESTS ==========

        @Test
        void testGetRenterRating_ExistingRating() throws Exception {
                session.setAttribute("userId", 1L);

                Booking pastBooking = new Booking();
                pastBooking.setId(1L);
                pastBooking.setRenterId(2L);
                pastBooking.setEndDate(LocalDate.now().minusDays(1));
                pastBooking.setItem(item1);

                Rating existingRating = new Rating(1L, RatingType.RENTER, 2L, 4, "Good renter");
                existingRating.setId(1L);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(pastBooking));
                given(ratingService.getRatingBySenderIdAndRatedInfo(1L, RatingType.RENTER, 2L))
                                .willReturn(Optional.of(existingRating));

                mockMvc.perform(get("/api/owner/dashboard/bookings/1/renter-rating")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.exists").value(true))
                                .andExpect(jsonPath("$.rating.rate").value(4))
                                .andExpect(jsonPath("$.rating.comment").value("Good renter"));

                verify(ratingService, times(1)).getRatingBySenderIdAndRatedInfo(1L, RatingType.RENTER, 2L);
        }

        @Test
        void testGetRenterRating_NoExistingRating() throws Exception {
                session.setAttribute("userId", 1L);

                Booking pastBooking = new Booking();
                pastBooking.setId(1L);
                pastBooking.setRenterId(2L);
                pastBooking.setEndDate(LocalDate.now().minusDays(1));
                pastBooking.setItem(item1);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(pastBooking));
                given(ratingService.getRatingBySenderIdAndRatedInfo(1L, RatingType.RENTER, 2L))
                                .willReturn(Optional.empty());

                mockMvc.perform(get("/api/owner/dashboard/bookings/1/renter-rating")
                                .session(session))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.exists").value(false));

                verify(ratingService, times(1)).getRatingBySenderIdAndRatedInfo(1L, RatingType.RENTER, 2L);
        }

        @Test
        void testGetRenterRating_NotLoggedIn() throws Exception {
                mockMvc.perform(get("/api/owner/dashboard/bookings/1/renter-rating"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("User not logged in"));

                verify(ratingService, never()).getRatingBySenderIdAndRatedInfo(anyLong(), any(), anyLong());
        }

        @Test
        void testGetRenterRating_NotOwnerRole() throws Exception {
                session.setAttribute("userId", 2L);
                given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

                mockMvc.perform(get("/api/owner/dashboard/bookings/1/renter-rating")
                                .session(session))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("User is not an owner"));

                verify(ratingService, never()).getRatingBySenderIdAndRatedInfo(anyLong(), any(), anyLong());
        }

        @Test
        void testGetRenterRating_BookingNotFound() throws Exception {
                session.setAttribute("userId", 1L);
                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(99L)).willReturn(Optional.empty());

                mockMvc.perform(get("/api/owner/dashboard/bookings/99/renter-rating")
                                .session(session))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Booking not found"));

                verify(ratingService, never()).getRatingBySenderIdAndRatedInfo(anyLong(), any(), anyLong());
        }

        @Test
        void testGetRenterRating_NotOwnerOfBooking() throws Exception {
                session.setAttribute("userId", 1L);

                Booking booking = new Booking();
                booking.setId(1L);
                booking.setRenterId(2L);
                Item otherOwnerItem = new Item("Other Item", "Description", 10.0, category, 4.5, "Location", 99L);
                booking.setItem(otherOwnerItem);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(booking));

                mockMvc.perform(get("/api/owner/dashboard/bookings/1/renter-rating")
                                .session(session))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("Booking does not belong to this owner"));

                verify(ratingService, never()).getRatingBySenderIdAndRatedInfo(anyLong(), any(), anyLong());
        }

        // ========== RATE RENTER TESTS ==========

        @Test
        void testRateRenter_Success() throws Exception {
                session.setAttribute("userId", 1L);

                Booking pastBooking = new Booking();
                pastBooking.setId(1L);
                pastBooking.setRenterId(2L);
                pastBooking.setEndDate(LocalDate.now().minusDays(1));
                pastBooking.setItem(item1);

                Rating createdRating = new Rating(1L, RatingType.RENTER, 2L, 5, "Excellent renter!");
                createdRating.setId(1L);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(pastBooking));
                given(ratingService.createRating(1L, RatingType.RENTER, 2L, 5, "Excellent renter!"))
                                .willReturn(createdRating);

                String requestBody = "{\"rate\":5,\"comment\":\"Excellent renter!\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/rate-renter")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("Renter rated successfully"))
                                .andExpect(jsonPath("$.ratingId").value(1));

                verify(ratingService, times(1)).createRating(1L, RatingType.RENTER, 2L, 5, "Excellent renter!");
        }

        @Test
        void testRateRenter_SuccessWithoutComment() throws Exception {
                session.setAttribute("userId", 1L);

                Booking pastBooking = new Booking();
                pastBooking.setId(1L);
                pastBooking.setRenterId(2L);
                pastBooking.setEndDate(LocalDate.now().minusDays(1));
                pastBooking.setItem(item1);

                Rating createdRating = new Rating(1L, RatingType.RENTER, 2L, 4, null);
                createdRating.setId(2L);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(pastBooking));
                given(ratingService.createRating(1L, RatingType.RENTER, 2L, 4, null))
                                .willReturn(createdRating);

                String requestBody = "{\"rate\":4,\"comment\":null}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/rate-renter")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("Renter rated successfully"))
                                .andExpect(jsonPath("$.ratingId").value(2));

                verify(ratingService, times(1)).createRating(1L, RatingType.RENTER, 2L, 4, null);
        }

        @Test
        void testRateRenter_NotLoggedIn() throws Exception {
                String requestBody = "{\"rate\":5,\"comment\":\"Great!\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/rate-renter")
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("User not logged in"));

                verify(ratingService, never()).createRating(anyLong(), any(), anyLong(), anyInt(), anyString());
        }

        @Test
        void testRateRenter_NotOwnerRole() throws Exception {
                session.setAttribute("userId", 2L);
                given(userService.getUserById(2L)).willReturn(Optional.of(renterUser));

                String requestBody = "{\"rate\":5,\"comment\":\"Great!\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/rate-renter")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("User is not an owner"));

                verify(ratingService, never()).createRating(anyLong(), any(), anyLong(), anyInt(), anyString());
        }

        @Test
        void testRateRenter_BookingNotFound() throws Exception {
                session.setAttribute("userId", 1L);
                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(99L)).willReturn(Optional.empty());

                String requestBody = "{\"rate\":5,\"comment\":\"Great!\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/99/rate-renter")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").value("Booking not found"));

                verify(ratingService, never()).createRating(anyLong(), any(), anyLong(), anyInt(), anyString());
        }

        @Test
        void testRateRenter_NotOwnerOfBooking() throws Exception {
                session.setAttribute("userId", 1L);

                Booking booking = new Booking();
                booking.setId(1L);
                booking.setRenterId(2L);
                booking.setEndDate(LocalDate.now().minusDays(1));
                Item otherOwnerItem = new Item("Other Item", "Description", 10.0, category, 4.5, "Location", 99L);
                booking.setItem(otherOwnerItem);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(booking));

                String requestBody = "{\"rate\":5,\"comment\":\"Great!\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/rate-renter")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").value("Booking does not belong to this owner"));

                verify(ratingService, never()).createRating(anyLong(), any(), anyLong(), anyInt(), anyString());
        }

        @Test
        void testRateRenter_FutureRental() throws Exception {
                session.setAttribute("userId", 1L);

                Booking futureBooking = new Booking();
                futureBooking.setId(1L);
                futureBooking.setRenterId(2L);
                futureBooking.setEndDate(LocalDate.now().plusDays(5));
                futureBooking.setItem(item1);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(futureBooking));

                String requestBody = "{\"rate\":5,\"comment\":\"Great!\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/rate-renter")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error")
                                                .value("Cannot rate renter for ongoing or future rentals"));

                verify(ratingService, never()).createRating(anyLong(), any(), anyLong(), anyInt(), anyString());
        }

        @Test
        void testRateRenter_OngoingRental() throws Exception {
                session.setAttribute("userId", 1L);

                Booking ongoingBooking = new Booking();
                ongoingBooking.setId(1L);
                ongoingBooking.setRenterId(2L);
                ongoingBooking.setEndDate(LocalDate.now()); // Ends today
                ongoingBooking.setItem(item1);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(ongoingBooking));

                String requestBody = "{\"rate\":5,\"comment\":\"Great!\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/rate-renter")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error")
                                                .value("Cannot rate renter for ongoing or future rentals"));

                verify(ratingService, never()).createRating(anyLong(), any(), anyLong(), anyInt(), anyString());
        }

        @Test
        void testRateRenter_MissingRating() throws Exception {
                session.setAttribute("userId", 1L);

                Booking pastBooking = new Booking();
                pastBooking.setId(1L);
                pastBooking.setRenterId(2L);
                pastBooking.setEndDate(LocalDate.now().minusDays(1));
                pastBooking.setItem(item1);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(pastBooking));

                String requestBody = "{\"comment\":\"Great!\"}"; // Missing rate

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/rate-renter")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("Rating is required"));

                verify(ratingService, never()).createRating(anyLong(), any(), anyLong(), anyInt(), anyString());
        }

        @Test
        void testRateRenter_RatingServiceThrowsException() throws Exception {
                session.setAttribute("userId", 1L);

                Booking pastBooking = new Booking();
                pastBooking.setId(1L);
                pastBooking.setRenterId(2L);
                pastBooking.setEndDate(LocalDate.now().minusDays(1));
                pastBooking.setItem(item1);

                given(userService.getUserById(1L)).willReturn(Optional.of(ownerUser));
                given(bookingRepository.findById(1L)).willReturn(Optional.of(pastBooking));
                given(ratingService.createRating(1L, RatingType.RENTER, 2L, 5, "Great!"))
                                .willThrow(new IllegalArgumentException(
                                                "You can only rate renters you have completed bookings with."));

                String requestBody = "{\"rate\":5,\"comment\":\"Great!\"}";

                mockMvc.perform(post("/api/owner/dashboard/bookings/1/rate-renter")
                                .session(session)
                                .contentType("application/json")
                                .content(requestBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error")
                                                .value("You can only rate renters you have completed bookings with."));

                verify(ratingService, times(1)).createRating(1L, RatingType.RENTER, 2L, 5, "Great!");
        }
}
