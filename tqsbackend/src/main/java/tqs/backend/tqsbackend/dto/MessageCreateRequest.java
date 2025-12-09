package tqs.backend.tqsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageCreateRequest {

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotBlank(message = "Message content is required")
    private String content;

    private Long itemId;

    public MessageCreateRequest(Long receiverId, String content) {
        this.receiverId = receiverId;
        this.content = content;
    }

    public MessageCreateRequest(Long receiverId, String content, Long itemId) {
        this(receiverId, content);
        this.itemId = itemId;
    }
}
