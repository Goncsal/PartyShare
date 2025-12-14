package tqs.backend.tqsbackend.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tqs.backend.tqsbackend.dto.BookingCreateRequest;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.Item;
import java.util.List;
import tqs.backend.tqsbackend.exception.AvailabilityException;
import tqs.backend.tqsbackend.exception.BookingValidationException;
import tqs.backend.tqsbackend.service.BookingService;
import tqs.backend.tqsbackend.service.ItemService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    private final ItemService itemService;

    private final tqs.backend.tqsbackend.service.UserService userService;

    @GetMapping("/rent/{itemId}")
    public String showRentForm(@PathVariable Long itemId, Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/users/login";
        }

        if (!model.containsAttribute("bookingRequest")) {
            BookingCreateRequest request = new BookingCreateRequest();
            request.setItemId(itemId);
            model.addAttribute("bookingRequest", request);
        }

        Item item = itemService.getItemById(itemId);
        if (item == null) {
            model.addAttribute("error", "Item not found");
            return "bookings/rent_item";
        }

        model.addAttribute("item", item);
        return "bookings/rent_item";
    }

    private final org.springframework.validation.SmartValidator validator;

    @PostMapping
    public String createBooking(@ModelAttribute("bookingRequest") BookingCreateRequest request,
            BindingResult bindingResult, RedirectAttributes redirectAttributes, HttpSession session) {

        Long renterId = (Long) session.getAttribute("userId");
        if (renterId == null) {
            return "redirect:/users/login";
        }
        request.setRenterId(renterId);

        validator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid booking data");
            redirectAttributes.addFlashAttribute("bookingRequest", request);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.bookingRequest",
                    bindingResult);
            return "redirect:/bookings/rent/" + request.getItemId();
        }

        try {
            bookingService.createBooking(request);
            redirectAttributes.addFlashAttribute("success", "Booking request sent! Waiting for owner approval.");
            return "redirect:/bookings";
        } catch (AvailabilityException e) {
            redirectAttributes.addFlashAttribute("error", "Dates unavailable for this item");
        } catch (BookingValidationException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/bookings/rent/" + request.getItemId();
    }

    @GetMapping
    public String getUserBookings(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        List<Booking> bookings = bookingService.getBookingsForRenter(userId);
        model.addAttribute("bookings", bookings);
        return "bookings/list";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        try {
            bookingService.cancelBooking(id, userId);
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully");
        } catch (BookingValidationException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/bookings";
    }

    @GetMapping("/requests")
    public String getBookingRequests(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        tqs.backend.tqsbackend.entity.User user = userService.getUserById(userId).orElse(null);
        if (user == null || user.getRole() != tqs.backend.tqsbackend.entity.UserRoles.OWNER) {
            return "redirect:/items/search";
        }

        List<Booking> bookings = bookingService.getPendingBookingsByOwner(userId);
        model.addAttribute("bookings", bookings);
        return "bookings/requests";
    }

    @PostMapping("/{id}/accept")
    public String acceptBooking(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }
        bookingService.acceptBooking(id, userId);
        return "redirect:/bookings/requests";
    }

    @PostMapping("/{id}/decline")
    public String declineBooking(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }
        bookingService.declineBooking(id, userId);
        return "redirect:/bookings/requests";
    }

    @PostMapping("/{id}/counter-offer")
    public String counterOfferBooking(@PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam Double price, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }
        bookingService.counterOfferBooking(id, price, userId);
        return "redirect:/bookings/requests";
    }

    @PostMapping("/{id}/accept-counter-offer")
    public String acceptCounterOffer(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }
        bookingService.acceptCounterOffer(id, userId);
        return "redirect:/bookings";
    }

    @PostMapping("/{id}/decline-counter-offer")
    public String declineCounterOffer(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }
        bookingService.declineCounterOffer(id, userId);
        return "redirect:/bookings";
    }
}
