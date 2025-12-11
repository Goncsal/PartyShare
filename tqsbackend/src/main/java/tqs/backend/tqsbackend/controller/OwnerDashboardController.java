package tqs.backend.tqsbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import tqs.backend.tqsbackend.entity.Booking;
import tqs.backend.tqsbackend.entity.BookingStatus;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.service.ItemService;
import tqs.backend.tqsbackend.service.UserService;
import tqs.backend.tqsbackend.repository.BookingRepository;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/owner/dashboard")
@RequiredArgsConstructor
public class OwnerDashboardController {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    @GetMapping
    public String showDashboard(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/users/login";
        }

        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty() || userOpt.get().getRole() != UserRoles.OWNER) {
            model.addAttribute("error", "Access denied. Owner role required.");
            return "error";
        }

        User user = userOpt.get();
        List<Item> items = itemService.findByOwnerId(userId);

        // Fetch upcoming rentals (bookings with endDate >= today)
        List<Booking> upcomingRentals = bookingRepository
                .findByItem_OwnerIdAndStatusInAndEndDateGreaterThanEqualOrderByStartDateAsc(
                        userId,
                        Arrays.asList(BookingStatus.ACCEPTED, BookingStatus.REQUESTED),
                        LocalDate.now());

        // Fetch past rentals (bookings with endDate < today)
        List<Booking> pastRentals = bookingRepository
                .findByItem_OwnerIdAndStatusInAndEndDateLessThanOrderByStartDateDesc(
                        userId,
                        Arrays.asList(BookingStatus.ACCEPTED, BookingStatus.REQUESTED),
                        LocalDate.now());

        model.addAttribute("user", user);
        model.addAttribute("items", items);
        model.addAttribute("upcomingRentals", upcomingRentals);
        model.addAttribute("pastRentals", pastRentals);
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("userName", user.getName());

        return "dashboard/owner";
    }

}
