package tqs.backend.tqsbackend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import tqs.backend.tqsbackend.dto.MessageCreateRequest;
import tqs.backend.tqsbackend.entity.Category;
import tqs.backend.tqsbackend.entity.Item;
import tqs.backend.tqsbackend.entity.Message;
import tqs.backend.tqsbackend.service.ItemService;
import tqs.backend.tqsbackend.service.MessageService;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private ItemService itemService;

    private Item createTestItem() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Test");

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setOwnerId(2L);
        item.setCategory(category);
        return item;
    }

    @Test
    void showMessageForm_NotLoggedIn_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/messages/new")
                .param("receiverId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void showMessageForm_LoggedIn_ReturnsForm() throws Exception {
        Item item = createTestItem();

        when(itemService.getItemById(1L)).thenReturn(item);
        when(messageService.getConversation(1L, 2L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/messages/new")
                .sessionAttr("userId", 1L)
                .param("receiverId", "2")
                .param("itemId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("messages"))
                .andExpect(model().attributeExists("item", "messages"));
    }

    @Test
    void showMessageForm_WithoutItem_ReturnsForm() throws Exception {
        when(messageService.getConversation(1L, 2L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/messages/new")
                .sessionAttr("userId", 1L)
                .param("receiverId", "2"))
                .andExpect(status().isOk())
                .andExpect(view().name("messages"))
                .andExpect(model().attributeExists("messages"));
    }

    @Test
    void sendMessage_NotLoggedIn_RedirectsToLogin() throws Exception {
        mockMvc.perform(post("/messages")
                .param("receiverId", "2")
                .param("content", "Hello"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void sendMessage_LoggedIn_SendsAndRedirects() throws Exception {
        Message message = new Message(1L, 2L, "Hello");
        message.setId(1L);

        when(messageService.sendMessage(eq(1L), any(MessageCreateRequest.class))).thenReturn(message);

        mockMvc.perform(post("/messages")
                .sessionAttr("userId", 1L)
                .param("receiverId", "2")
                .param("content", "Hello"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/messages/new*"));
    }

    @Test
    void sendMessage_WithItemId_IncludesInRedirect() throws Exception {
        Message message = new Message(1L, 2L, "Hello", 1L);
        message.setId(1L);

        when(messageService.sendMessage(eq(1L), any(MessageCreateRequest.class))).thenReturn(message);

        mockMvc.perform(post("/messages")
                .sessionAttr("userId", 1L)
                .param("receiverId", "2")
                .param("content", "Hello")
                .param("itemId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/messages/new*"));
    }

    @Test
    void listMessages_NotLoggedIn_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/messages"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/login"));
    }

    @Test
    void listMessages_LoggedIn_ReturnsMessages() throws Exception {
        Message msg = new Message(1L, 2L, "Hi");
        msg.setId(1L);

        when(messageService.getMessagesForUser(1L)).thenReturn(Collections.singletonList(msg));

        mockMvc.perform(get("/messages")
                .sessionAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("messages"))
                .andExpect(model().attributeExists("messages"));
    }
}
