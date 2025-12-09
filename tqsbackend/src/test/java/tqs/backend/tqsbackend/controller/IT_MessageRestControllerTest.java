package tqs.backend.tqsbackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tqs.backend.tqsbackend.dto.MessageCreateRequest;
import tqs.backend.tqsbackend.entity.Message;
import tqs.backend.tqsbackend.service.MessageService;

/**
 * TDD tests for Message REST endpoints (US 1.5)
 */
@WebMvcTest(MessageRestController.class)
@Import(IT_MessageRestControllerTest.TestConfig.class)
class IT_MessageRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageService messageService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MessageService messageService() {
            return Mockito.mock(MessageService.class);
        }
    }

    @Test
    @DisplayName("POST /api/messages sends message successfully")
    void sendMessage_success() throws Exception {
        Long senderId = 1L;
        Message message = new Message(senderId, 2L, "Hello!");
        message.setId(1L);
        message.setSentAt(LocalDateTime.now());
        
        when(messageService.sendMessage(eq(senderId), any(MessageCreateRequest.class))).thenReturn(message);

        mockMvc.perform(post("/api/messages")
                .param("senderId", senderId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"receiverId\": 2, \"content\": \"Hello!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.senderId").value(senderId))
                .andExpect(jsonPath("$.receiverId").value(2))
                .andExpect(jsonPath("$.content").value("Hello!"));

        verify(messageService).sendMessage(eq(senderId), any(MessageCreateRequest.class));
    }

    @Test
    @DisplayName("GET /api/messages returns user messages")
    void getMessages_success() throws Exception {
        Long userId = 1L;
        Message msg1 = new Message(userId, 2L, "Message 1");
        msg1.setId(1L);
        msg1.setSentAt(LocalDateTime.now());
        Message msg2 = new Message(3L, userId, "Message 2");
        msg2.setId(2L);
        msg2.setSentAt(LocalDateTime.now());
        
        when(messageService.getMessagesForUser(userId)).thenReturn(Arrays.asList(msg1, msg2));

        mockMvc.perform(get("/api/messages")
                .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(messageService).getMessagesForUser(userId);
    }

    @Test
    @DisplayName("GET /api/messages/conversation returns conversation between users")
    void getConversation_success() throws Exception {
        Long userId1 = 1L;
        Long userId2 = 2L;
        Message msg1 = new Message(userId1, userId2, "Hi");
        msg1.setId(1L);
        msg1.setSentAt(LocalDateTime.now().minusMinutes(10));
        Message msg2 = new Message(userId2, userId1, "Hello");
        msg2.setId(2L);
        msg2.setSentAt(LocalDateTime.now());
        
        when(messageService.getConversation(userId1, userId2)).thenReturn(Arrays.asList(msg1, msg2));

        mockMvc.perform(get("/api/messages/conversation/{otherUserId}", userId2)
                .param("userId", userId1.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(messageService).getConversation(userId1, userId2);
    }
}
