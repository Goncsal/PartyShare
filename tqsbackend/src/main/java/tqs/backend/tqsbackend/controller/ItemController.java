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
import tqs.backend.tqsbackend.service.CategoryService;
import tqs.backend.tqsbackend.service.ItemService;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    private final CategoryService categoryService;

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
        model.addAttribute("categories", categories);
        model.addAttribute("q", q);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minRating", minRating);
        model.addAttribute("location", location);

        model.addAttribute("isLoggedIn", session.getAttribute("userId") != null);
        model.addAttribute("userName", session.getAttribute("userName"));

        return "search";
    }

    @GetMapping("/{id}")
    public String getItemDetails(@PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        model.addAttribute("item", item);
        return "item_details";
    }

    @GetMapping("/new")
    public String showNewItemForm(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("item", new Item());
        model.addAttribute("categories", categoryService.getAllCategories());
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

        List<Item> items = itemService.getItemsByOwner(userId);
        model.addAttribute("items", items);
        return "items/my_items";
    }
}
