package tqs.backend.tqsbackend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
import tqs.backend.tqsbackend.exception.AvailabilityException;
import tqs.backend.tqsbackend.exception.BookingValidationException;
import tqs.backend.tqsbackend.exception.PaymentException;
import tqs.backend.tqsbackend.service.BookingService;
import tqs.backend.tqsbackend.service.ItemService;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemService itemService;

    @GetMapping("/rent/{itemId}")
    public String showRentForm(@PathVariable Long itemId, Model model) {
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

    @PostMapping
    public String createBooking(@ModelAttribute("bookingRequest") @Valid BookingCreateRequest request,
            BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid booking data");
            redirectAttributes.addFlashAttribute("bookingRequest", request);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.bookingRequest",
                    bindingResult);
            return "redirect:/bookings/rent/" + request.getItemId();
        }

        try {
            Booking booking = bookingService.createBooking(request);
            redirectAttributes.addFlashAttribute("success",
                    "Booking confirmed! Ref: " + booking.getPaymentReference());
        } catch (AvailabilityException e) {
            redirectAttributes.addFlashAttribute("error", "Dates unavailable for this item");
        } catch (PaymentException | BookingValidationException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/bookings/rent/" + request.getItemId();
    }
}
