package tqs.backend.tqsbackend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConversationSummaryDTOTest {

    @Test
    @DisplayName("Create ConversationSummaryDTO with all fields")
    void createConversationSummary_allFields() {
        Long otherUserId = 2L;
        String otherUserName = "John Doe";
        String lastMessageContent = "Hello, how are you?";
        LocalDateTime lastMessageTime = LocalDateTime.now();
        boolean lastMessageFromMe = true;
        Long itemId = 10L;

        ConversationSummaryDTO dto = new ConversationSummaryDTO(
                otherUserId, otherUserName, lastMessageContent,
                lastMessageTime, lastMessageFromMe, itemId);

        assertThat(dto.getOtherUserId()).isEqualTo(otherUserId);
        assertThat(dto.getOtherUserName()).isEqualTo(otherUserName);
        assertThat(dto.getLastMessageContent()).isEqualTo(lastMessageContent);
        assertThat(dto.getLastMessageTime()).isEqualTo(lastMessageTime);
        assertThat(dto.isLastMessageFromMe()).isEqualTo(lastMessageFromMe);
        assertThat(dto.getItemId()).isEqualTo(itemId);
    }

    @Test
    @DisplayName("Create ConversationSummaryDTO with setters")
    void createConversationSummary_withSetters() {
        ConversationSummaryDTO dto = new ConversationSummaryDTO();

        dto.setOtherUserId(3L);
        dto.setOtherUserName("Jane Smith");
        dto.setLastMessageContent("Test message");
        LocalDateTime time = LocalDateTime.now();
        dto.setLastMessageTime(time);
        dto.setLastMessageFromMe(false);
        dto.setItemId(20L);

        assertThat(dto.getOtherUserId()).isEqualTo(3L);
        assertThat(dto.getOtherUserName()).isEqualTo("Jane Smith");
        assertThat(dto.getLastMessageContent()).isEqualTo("Test message");
        assertThat(dto.getLastMessageTime()).isEqualTo(time);
        assertThat(dto.isLastMessageFromMe()).isFalse();
        assertThat(dto.getItemId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Create ConversationSummaryDTO without item ID")
    void createConversationSummary_withoutItemId() {
        ConversationSummaryDTO dto = new ConversationSummaryDTO(
                2L, "User", "Message", LocalDateTime.now(), false, null);

        assertThat(dto.getItemId()).isNull();
    }
}
