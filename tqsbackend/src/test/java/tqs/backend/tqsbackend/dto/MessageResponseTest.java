package tqs.backend.tqsbackend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class MessageResponseTest {

    @Test
    void constructor_SetsAllValues() {
        LocalDateTime now = LocalDateTime.now();
        
        MessageResponse response = new MessageResponse(1L, 2L, 3L, 4L, "Hello", now, true);
        
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSenderId()).isEqualTo(2L);
        assertThat(response.getReceiverId()).isEqualTo(3L);
        assertThat(response.getItemId()).isEqualTo(4L);
        assertThat(response.getContent()).isEqualTo("Hello");
        assertThat(response.getSentAt()).isEqualTo(now);
        assertThat(response.isRead()).isTrue();
    }

    @Test
    void response_WithNullItemId() {
        LocalDateTime now = LocalDateTime.now();
        
        MessageResponse response = new MessageResponse(1L, 2L, 3L, null, "Hello", now, false);
        
        assertThat(response.getItemId()).isNull();
        assertThat(response.isRead()).isFalse();
    }

    @Test
    void differentResponses_HaveDifferentValues() {
        LocalDateTime now = LocalDateTime.now();
        
        MessageResponse r1 = new MessageResponse(1L, 2L, 3L, 4L, "Hi", now, false);
        MessageResponse r2 = new MessageResponse(2L, 3L, 4L, 5L, "Bye", now, true);
        
        assertThat(r1.getId()).isNotEqualTo(r2.getId());
        assertThat(r1.getContent()).isNotEqualTo(r2.getContent());
    }
}
