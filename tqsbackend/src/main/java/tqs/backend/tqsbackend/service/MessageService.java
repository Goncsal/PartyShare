package tqs.backend.tqsbackend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tqs.backend.tqsbackend.dto.ConversationSummaryDTO;
import tqs.backend.tqsbackend.dto.MessageCreateRequest;
import tqs.backend.tqsbackend.entity.Message;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.exception.BookingValidationException;
import tqs.backend.tqsbackend.repository.MessageRepository;
import tqs.backend.tqsbackend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public Message sendMessage(Long senderId, MessageCreateRequest request) {
        if (senderId.equals(request.getReceiverId())) {
            throw new BookingValidationException("You cannot send message to yourself");
        }

        Message message = new Message(senderId, request.getReceiverId(), request.getContent(), request.getItemId());
        return messageRepository.save(message);
    }

    public List<Message> getConversation(Long userId1, Long userId2) {
        return messageRepository.findConversation(userId1, userId2);
    }

    public List<Message> getMessagesForUser(Long userId) {
        return messageRepository.findAllForUser(userId);
    }

    public List<ConversationSummaryDTO> getConversationsList(Long userId) {
        List<Message> allMessages = messageRepository.findAllForUser(userId);

        // Group messages by conversation partner
        Map<Long, Message> latestMessageByPartner = new HashMap<>();

        for (Message msg : allMessages) {
            Long partnerId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();

            // Keep only the most recent message for each partner
            if (!latestMessageByPartner.containsKey(partnerId) ||
                    msg.getSentAt().isAfter(latestMessageByPartner.get(partnerId).getSentAt())) {
                latestMessageByPartner.put(partnerId, msg);
            }
        }

        // Build conversation summaries
        List<ConversationSummaryDTO> conversations = new ArrayList<>();
        for (Map.Entry<Long, Message> entry : latestMessageByPartner.entrySet()) {
            Long partnerId = entry.getKey();
            Message lastMessage = entry.getValue();

            // Fetch partner's name
            String partnerName = userRepository.findById(partnerId)
                    .map(User::getName)
                    .orElse("Unknown User");

            boolean lastMessageFromMe = lastMessage.getSenderId().equals(userId);

            ConversationSummaryDTO summary = new ConversationSummaryDTO(
                    partnerId,
                    partnerName,
                    lastMessage.getContent(),
                    lastMessage.getSentAt(),
                    lastMessageFromMe,
                    lastMessage.getItemId());

            conversations.add(summary);
        }

        // Sort by most recent message first
        conversations.sort((a, b) -> b.getLastMessageTime().compareTo(a.getLastMessageTime()));

        return conversations;
    }
}
