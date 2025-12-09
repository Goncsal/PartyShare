package tqs.backend.tqsbackend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import tqs.backend.tqsbackend.dto.MessageCreateRequest;
import tqs.backend.tqsbackend.dto.MessageResponse;
import tqs.backend.tqsbackend.entity.Message;
import tqs.backend.tqsbackend.service.MessageService;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageRestController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestParam Long senderId,
            @RequestBody @Valid MessageCreateRequest request) {
        Message message = messageService.sendMessage(senderId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(message));
    }

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getMessages(@RequestParam Long userId) {
        List<Message> messages = messageService.getMessagesForUser(userId);
        return ResponseEntity.ok(messages.stream().map(this::toResponse).toList());
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<List<MessageResponse>> getConversation(
            @RequestParam Long userId,
            @PathVariable Long otherUserId) {
        List<Message> messages = messageService.getConversation(userId, otherUserId);
        return ResponseEntity.ok(messages.stream().map(this::toResponse).toList());
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getItemId(),
                message.getContent(),
                message.getSentAt(),
                message.isRead());
    }
}
