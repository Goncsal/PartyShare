package tqs.backend.tqsbackend.dto;

import java.time.LocalDateTime;

public class ConversationSummaryDTO {
    private Long otherUserId;
    private String otherUserName;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private boolean lastMessageFromMe;
    private Long itemId;

    public ConversationSummaryDTO() {
    }

    public ConversationSummaryDTO(Long otherUserId, String otherUserName, String lastMessageContent,
            LocalDateTime lastMessageTime, boolean lastMessageFromMe, Long itemId) {
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.lastMessageContent = lastMessageContent;
        this.lastMessageTime = lastMessageTime;
        this.lastMessageFromMe = lastMessageFromMe;
        this.itemId = itemId;
    }

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getLastMessageContent() {
        return lastMessageContent;
    }

    public void setLastMessageContent(String lastMessageContent) {
        this.lastMessageContent = lastMessageContent;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public boolean isLastMessageFromMe() {
        return lastMessageFromMe;
    }

    public void setLastMessageFromMe(boolean lastMessageFromMe) {
        this.lastMessageFromMe = lastMessageFromMe;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
}
