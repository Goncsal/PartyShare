package tqs.backend.tqsbackend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import tqs.backend.tqsbackend.dto.MessageCreateRequest;
import tqs.backend.tqsbackend.entity.Message;
import tqs.backend.tqsbackend.exception.BookingValidationException;
import tqs.backend.tqsbackend.repository.MessageRepository;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

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
}
