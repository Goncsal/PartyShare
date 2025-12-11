package tqs.backend.tqsbackend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tqs.backend.tqsbackend.dto.ConversationSummaryDTO;
import tqs.backend.tqsbackend.dto.MessageCreateRequest;
import tqs.backend.tqsbackend.entity.Message;
import tqs.backend.tqsbackend.entity.User;
import tqs.backend.tqsbackend.entity.UserRoles;
import tqs.backend.tqsbackend.exception.BookingValidationException;
import tqs.backend.tqsbackend.repository.MessageRepository;
import tqs.backend.tqsbackend.repository.UserRepository;

/**
 * TDD tests for Message Owner functionality (US 1.5)
 */
@ExtendWith(MockitoExtension.class)
class IT_MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

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

    @Test
    @DisplayName("Get conversations list with multiple partners")
    void getConversationsList_multiplePartners() {
        Long userId = 1L;
        Long partner1Id = 2L;
        Long partner2Id = 3L;

        // Create messages with different partners
        Message msg1 = new Message(userId, partner1Id, "Hello partner 1");
        msg1.setId(1L);
        msg1.setSentAt(LocalDateTime.now().minusHours(2));

        Message msg2 = new Message(partner1Id, userId, "Reply from partner 1");
        msg2.setId(2L);
        msg2.setSentAt(LocalDateTime.now().minusHours(1));

        Message msg3 = new Message(userId, partner2Id, "Hello partner 2");
        msg3.setId(3L);
        msg3.setSentAt(LocalDateTime.now().minusMinutes(30));

        when(messageRepository.findAllForUser(userId)).thenReturn(Arrays.asList(msg1, msg2, msg3));

        User partner1 = new User("Partner One", "partner1@example.com", "pass", UserRoles.OWNER);
        partner1.setId(partner1Id);
        User partner2 = new User("Partner Two", "partner2@example.com", "pass", UserRoles.OWNER);
        partner2.setId(partner2Id);

        when(userRepository.findById(partner1Id)).thenReturn(Optional.of(partner1));
        when(userRepository.findById(partner2Id)).thenReturn(Optional.of(partner2));

        List<ConversationSummaryDTO> result = messageService.getConversationsList(userId);

        assertThat(result).hasSize(2);

        // Should be ordered by most recent
        assertThat(result.get(0).getOtherUserId()).isEqualTo(partner2Id);
        assertThat(result.get(0).getOtherUserName()).isEqualTo("Partner Two");
        assertThat(result.get(0).getLastMessageContent()).isEqualTo("Hello partner 2");
        assertThat(result.get(0).isLastMessageFromMe()).isTrue();

        assertThat(result.get(1).getOtherUserId()).isEqualTo(partner1Id);
        assertThat(result.get(1).getOtherUserName()).isEqualTo("Partner One");
        assertThat(result.get(1).getLastMessageContent()).isEqualTo("Reply from partner 1");
        assertThat(result.get(1).isLastMessageFromMe()).isFalse();
    }

    @Test
    @DisplayName("Get conversations list returns empty when no messages")
    void getConversationsList_noMessages() {
        Long userId = 1L;

        when(messageRepository.findAllForUser(userId)).thenReturn(Arrays.asList());

        List<ConversationSummaryDTO> result = messageService.getConversationsList(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Get conversations list keeps only most recent message per partner")
    void getConversationsList_onlyMostRecentPerPartner() {
        Long userId = 1L;
        Long partnerId = 2L;

        // Multiple messages with same partner
        Message oldMsg = new Message(userId, partnerId, "Old message");
        oldMsg.setId(1L);
        oldMsg.setSentAt(LocalDateTime.now().minusDays(1));

        Message recentMsg = new Message(partnerId, userId, "Recent message");
        recentMsg.setId(2L);
        recentMsg.setSentAt(LocalDateTime.now());

        when(messageRepository.findAllForUser(userId)).thenReturn(Arrays.asList(oldMsg, recentMsg));

        User partner = new User("Partner", "partner@example.com", "pass", UserRoles.OWNER);
        partner.setId(partnerId);
        when(userRepository.findById(partnerId)).thenReturn(Optional.of(partner));

        List<ConversationSummaryDTO> result = messageService.getConversationsList(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastMessageContent()).isEqualTo("Recent message");
        assertThat(result.get(0).isLastMessageFromMe()).isFalse();
    }

    @Test
    @DisplayName("Get conversations list handles unknown user gracefully")
    void getConversationsList_unknownUser() {
        Long userId = 1L;
        Long unknownPartnerId = 999L;

        Message msg = new Message(userId, unknownPartnerId, "Hello");
        msg.setId(1L);
        msg.setSentAt(LocalDateTime.now());

        when(messageRepository.findAllForUser(userId)).thenReturn(Arrays.asList(msg));
        when(userRepository.findById(unknownPartnerId)).thenReturn(Optional.empty());

        List<ConversationSummaryDTO> result = messageService.getConversationsList(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOtherUserName()).isEqualTo("Unknown User");
    }
}
