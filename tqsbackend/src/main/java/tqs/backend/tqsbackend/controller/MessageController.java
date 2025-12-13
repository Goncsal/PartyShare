package tqs.backend.tqsbackend.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import tqs.backend.tqsbackend.dto.ConversationSummaryDTO;
import tqs.backend.tqsbackend.dto.MessageCreateRequest;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.Message;
import tqs.backend.tqsbackend.exception.BookingValidationException;
import tqs.backend.tqsbackend.service.ItemService;
import tqs.backend.tqsbackend.service.MessageService;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ItemService itemService;

    @GetMapping("/new")
    public String showNewMessageForm(
            @RequestParam Long receiverId,
            @RequestParam(required = false) Long itemId,
            Model model, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        model.addAttribute("receiverId", receiverId);
        model.addAttribute("userId", userId);

        if (itemId != null) {
            Item item = itemService.getItemById(itemId);
            model.addAttribute("item", item);
        }

        // Load existing conversation
        List<Message> messages = messageService.getConversation(userId, receiverId);
        model.addAttribute("messages", messages);

        return "messages";
    }

    @PostMapping
    public String sendMessage(
            @RequestParam Long receiverId,
            @RequestParam String content,
            @RequestParam(required = false) Long itemId,
            HttpSession session, RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        try {
            MessageCreateRequest request = new MessageCreateRequest(receiverId, content, itemId);
            messageService.sendMessage(userId, request);
            redirectAttributes.addFlashAttribute("success", "Message sent successfully!");
        } catch (BookingValidationException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        String redirectUrl = "/messages/new?receiverId=" + receiverId;
        if (itemId != null) {
            redirectUrl += "&itemId=" + itemId;
        }
        return "redirect:" + redirectUrl;
    }

    @GetMapping
    public String listMessages(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        List<Message> messages = messageService.getMessagesForUser(userId);
        model.addAttribute("messages", messages);
        model.addAttribute("userId", userId);

        return "messages";
    }

    @GetMapping("/conversations")
    public String listConversations(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/users/login";
        }

        List<ConversationSummaryDTO> conversations = messageService.getConversationsList(userId);
        model.addAttribute("conversations", conversations);
        model.addAttribute("userId", userId);
        model.addAttribute("userRole", session.getAttribute("userRole"));

        return "conversations_list";
    }
}
