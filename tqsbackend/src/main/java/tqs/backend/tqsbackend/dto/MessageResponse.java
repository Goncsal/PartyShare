package tqs.backend.tqsbackend.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long itemId;
    private String content;
    private LocalDateTime sentAt;
    private boolean isRead;
}
