package tqs.backend.tqsbackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tqs.backend.tqsbackend.dto.MessageCreateRequest;
import tqs.backend.tqsbackend.entity.Message;
import tqs.backend.tqsbackend.exception.BookingValidationException;
import tqs.backend.tqsbackend.repository.MessageRepository;

/**
 * TDD tests for Message Owner functionality (US 1.5)
 */
@ExtendWith(MockitoExtension.class)
class IT_MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    @Test
    @DisplayName("Send message successfully")
    void sendMessage_success() {
        Long senderId = 1L;
        Long receiverId = 2L;
        String content = "Hello, is this item available?";
        
        MessageCreateRequest request = new MessageCreateRequest(receiverId, content);
        
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        Message result = messageService.sendMessage(senderId, request);

        assertThat(result.getSenderId()).isEqualTo(senderId);
        assertThat(result.getReceiverId()).isEqualTo(receiverId);
        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getSentAt()).isNotNull();
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("Send message with item context")
    void sendMessage_withItemContext() {
        Long senderId = 1L;
        Long receiverId = 2L;
        Long itemId = 10L;
        String content = "About the lamp";
        
        MessageCreateRequest request = new MessageCreateRequest(receiverId, content, itemId);
        
        when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        Message result = messageService.sendMessage(senderId, request);

        assertThat(result.getItemId()).isEqualTo(itemId);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("Send message fails when sender equals receiver")
    void sendMessage_fails_whenSenderEqualsReceiver() {
        Long senderId = 1L;
        MessageCreateRequest request = new MessageCreateRequest(senderId, "Hello");

        assertThatThrownBy(() -> messageService.sendMessage(senderId, request))
                .isInstanceOf(BookingValidationException.class)
                .hasMessageContaining("cannot send message to yourself");
    }

    @Test
    @DisplayName("Get conversation between two users")
    void getConversation_success() {
        Long userId1 = 1L;
        Long userId2 = 2L;
        
        Message msg1 = new Message(userId1, userId2, "Hi");
        msg1.setId(1L);
        msg1.setSentAt(LocalDateTime.now().minusMinutes(10));
        
        Message msg2 = new Message(userId2, userId1, "Hello");
        msg2.setId(2L);
        msg2.setSentAt(LocalDateTime.now().minusMinutes(5));
        
        when(messageRepository.findConversation(userId1, userId2)).thenReturn(Arrays.asList(msg1, msg2));

        List<Message> result = messageService.getConversation(userId1, userId2);

        assertThat(result).hasSize(2);
        verify(messageRepository).findConversation(userId1, userId2);
    }

    @Test
    @DisplayName("Get all messages for user")
    void getMessagesForUser_success() {
        Long userId = 1L;
        
        Message msg1 = new Message(userId, 2L, "Message 1");
        msg1.setId(1L);
        Message msg2 = new Message(3L, userId, "Message 2");
        msg2.setId(2L);
        
        when(messageRepository.findAllForUser(userId)).thenReturn(Arrays.asList(msg1, msg2));

        List<Message> result = messageService.getMessagesForUser(userId);

        assertThat(result).hasSize(2);
        verify(messageRepository).findAllForUser(userId);
    }
}
