package tqs.backend.tqsbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.service.CategoryService;
import tqs.backend.tqsbackend.service.ItemService;
import tqs.backend.tqsbackend.service.UserService;

import java.util.Optional;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    private final CategoryService categoryService;

    private final UserService userService;

    private static final String CATEGORIES_ATTR = "categories";

    @GetMapping("/search")
    public String searchItems(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String location,
            Model model, HttpSession session) {

        Category cat = null;
        if (category != null && !category.isEmpty()) {
            cat = categoryService.getCategoryByName(category);
        }

        List<Item> items = itemService.searchItems(q, cat, minPrice, maxPrice, minRating, location);
        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("items", items);
        model.addAttribute(CATEGORIES_ATTR, categories);
        model.addAttribute("q", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minRating", minRating);
        model.addAttribute("location", location);

        Long userId = (Long) session.getAttribute("userId");
        model.addAttribute("isLoggedIn", userId != null);
        model.addAttribute("userName", session.getAttribute("userName"));

        // Add userRole for conditional UI elements
        if (userId != null) {
            Optional<User> userOpt = userService.getUserById(userId);
            userOpt.ifPresent(user -> model.addAttribute("userRole", user.getRole().toString()));
        }

        // Add owner information for each item
        java.util.Map<Long, User> ownerMap = new java.util.HashMap<>();
        for (Item item : items) {
            if (item.getOwnerId() != null && !ownerMap.containsKey(item.getOwnerId())) {
                userService.getUserById(item.getOwnerId()).ifPresent(owner -> ownerMap.put(item.getOwnerId(), owner));
            }
        }
        model.addAttribute("ownerMap", ownerMap);

        return "search";
    }

    @GetMapping("/{id}")
    public String getItemDetails(@PathVariable Long id, Model model, HttpSession session) {
        Item item = itemService.getItemById(id);
        model.addAttribute("item", item);

        // Add owner information
        if (item.getOwnerId() != null) {
            userService.getUserById(item.getOwnerId()).ifPresent(owner -> {
                model.addAttribute("owner", owner);
            });
        }

        // Add user role for conditional UI elements
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            Optional<User> userOpt = userService.getUserById(userId);
            userOpt.ifPresent(user -> model.addAttribute("userRole", user.getRole().toString()));
        }

        return "item_details";
    }

    @GetMapping("/new")
    public String showNewItemForm(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        tqs.backend.tqsbackend.entity.User user = userService.getUserById(userId).orElse(null);
        if (user == null || user.getRole() != tqs.backend.tqsbackend.entity.UserRoles.OWNER) {
            return "redirect:/items/search";
        }
        model.addAttribute("userRole", user.getRole().name());

        model.addAttribute("item", new Item());
        model.addAttribute(CATEGORIES_ATTR, categoryService.getAllCategories());
        return "items/new_item";
    }

    @org.springframework.web.bind.annotation.PostMapping
    public String createItem(
            @org.springframework.web.bind.annotation.ModelAttribute Item item,
            @RequestParam Long categoryId,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        tqs.backend.tqsbackend.entity.User user = userService.getUserById(userId).orElse(null);
        if (user == null || user.getRole() != tqs.backend.tqsbackend.entity.UserRoles.OWNER) {
            return "redirect:/items/search";
        }

        item.setOwnerId(userId);
        item.setCategory(categoryService.getCategoryById(categoryId));

        Item savedItem = itemService.saveItem(item);
        return "redirect:/items/" + savedItem.getId();
    }

    @GetMapping("/my-items")
    public String getMyItems(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        tqs.backend.tqsbackend.entity.User user = userService.getUserById(userId).orElse(null);
        if (user == null || user.getRole() != tqs.backend.tqsbackend.entity.UserRoles.OWNER) {
            return "redirect:/items/search";
        }
        model.addAttribute("userRole", user.getRole().name());

        List<Item> items = itemService.findByOwnerId(userId);
        model.addAttribute("items", items);
        return "items/my_items";
    }
    @GetMapping("/{id}/edit")
    public String showEditItemForm(@PathVariable Long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        Item item = itemService.getItemById(id);
        if (item == null) {
            return "redirect:/items/search";
        }

        tqs.backend.tqsbackend.entity.User user = userService.getUserById(userId).orElse(null);
        if (user == null || !item.getOwnerId().equals(userId)) {
             return "redirect:/items/search";
        }
        
        model.addAttribute("userRole", user.getRole().name());
        model.addAttribute("item", item);
        model.addAttribute(CATEGORIES_ATTR, categoryService.getAllCategories());
        return "items/edit_item";
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/edit")
    public String updateItem(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.ModelAttribute Item item,
            @RequestParam Long categoryId,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        try {
            item.setCategory(categoryService.getCategoryById(categoryId));
            itemService.updateItem(id, item, userId);
            return "redirect:/items/my-items";
        } catch (IllegalArgumentException e) {
            return "redirect:/items/search";
        }
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/toggle-status")
    public String toggleItemStatus(
            @PathVariable Long id,
            @RequestParam boolean active,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        try {
            if (active) {
                itemService.activateItem(id, userId);
            } else {
                itemService.deactivateItem(id, userId);
            }
            return "redirect:/items/my-items";
        } catch (IllegalArgumentException e) {
            return "redirect:/items/search";
        }
    }
}
