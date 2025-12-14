package tqs.backend.tqsbackend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tqs.backend.tqsbackend.dto.ConversationSummaryDTO;
import tqs.backend.tqsbackend.dto.MessageCreateRequest;
import tqs.backend.tqsbackend.entity.Message;
import tqs.backend.tqsbackend.service.MessageService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageRestController.class)
class MessageRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @Test
    void sendMessage_ReturnsCreated() throws Exception {
        Message message = createTestMessage();
        when(messageService.sendMessage(eq(1L), any(MessageCreateRequest.class))).thenReturn(message);

        mockMvc.perform(post("/api/messages")
                        .param("senderId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiverId\": 2, \"itemId\": 1, \"content\": \"Hello!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Test message"));
    }

    @Test
    void getMessages_ReturnsUserMessages() throws Exception {
        Message message = createTestMessage();
        when(messageService.getMessagesForUser(1L)).thenReturn(List.of(message));

        mockMvc.perform(get("/api/messages")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].content").value("Test message"));
    }

    @Test
    void getMessages_EmptyList_ReturnsEmpty() throws Exception {
        when(messageService.getMessagesForUser(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/messages")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getConversation_ReturnsMessages() throws Exception {
        Message message = createTestMessage();
        when(messageService.getConversation(1L, 2L)).thenReturn(List.of(message));

        mockMvc.perform(get("/api/messages/conversation/2")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getConversationsList_ReturnsConversations() throws Exception {
        ConversationSummaryDTO summary = new ConversationSummaryDTO(
                2L, "Other User", "Last message", LocalDateTime.now(), false, 1L
        );
        when(messageService.getConversationsList(1L)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/messages/conversations")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].otherUserId").value(2));
    }

    @Test
    void getConversationsList_Empty_ReturnsEmpty() throws Exception {
        when(messageService.getConversationsList(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/messages/conversations")
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    private Message createTestMessage() {
        Message message = new Message();
        message.setId(1L);
        message.setSenderId(1L);
        message.setReceiverId(2L);
        message.setItemId(1L);
        message.setContent("Test message");
        message.setSentAt(LocalDateTime.now());
        message.setRead(false);
        return message;
    }
}
