package tqs.backend.tqsbackend.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class MessageTest {

    @Test
    void constructor_WithThreeArgs_SetsValuesCorrectly() {
        Message message = new Message(1L, 2L, "Hello World");
        
        assertThat(message.getSenderId()).isEqualTo(1L);
        assertThat(message.getReceiverId()).isEqualTo(2L);
        assertThat(message.getContent()).isEqualTo("Hello World");
        assertThat(message.getSentAt()).isNotNull();
        assertThat(message.isRead()).isFalse();
    }

    @Test
    void constructor_WithFourArgs_SetsItemId() {
        Message message = new Message(1L, 2L, "Hello", 3L);
        
        assertThat(message.getSenderId()).isEqualTo(1L);
        assertThat(message.getReceiverId()).isEqualTo(2L);
        assertThat(message.getContent()).isEqualTo("Hello");
        assertThat(message.getItemId()).isEqualTo(3L);
    }

    @Test
    void gettersAndSetters_WorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        
        Message message = new Message();
        message.setId(1L);
        message.setSenderId(2L);
        message.setReceiverId(3L);
        message.setItemId(4L);
        message.setContent("Test content");
        message.setSentAt(now);
        message.setRead(true);

        assertThat(message.getId()).isEqualTo(1L);
        assertThat(message.getSenderId()).isEqualTo(2L);
        assertThat(message.getReceiverId()).isEqualTo(3L);
        assertThat(message.getItemId()).isEqualTo(4L);
        assertThat(message.getContent()).isEqualTo("Test content");
        assertThat(message.getSentAt()).isEqualTo(now);
        assertThat(message.isRead()).isTrue();
    }

    @Test
    void defaultValues() {
        Message message = new Message();
        
        assertThat(message.getId()).isNull();
        assertThat(message.getSenderId()).isNull();
        assertThat(message.getReceiverId()).isNull();
        assertThat(message.getContent()).isNull();
        assertThat(message.isRead()).isFalse();
    }

    @Test
    void messageWithoutItem_IsValid() {
        Message message = new Message(1L, 2L, "General message");
        
        assertThat(message.getItemId()).isNull();
        assertThat(message.getContent()).isNotEmpty();
    }
}
