package tqs.backend.tqsbackend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessageCreateRequestTest {

    @Test
    void constructorWithTwoArgs_WorksCorrectly() {
        MessageCreateRequest request = new MessageCreateRequest(2L, "Hello World");
        
        assertThat(request.getReceiverId()).isEqualTo(2L);
        assertThat(request.getContent()).isEqualTo("Hello World");
        assertThat(request.getItemId()).isNull();
    }

    @Test
    void constructorWithThreeArgs_IncludesItemId() {
        MessageCreateRequest request = new MessageCreateRequest(2L, "Hello", 3L);
        
        assertThat(request.getReceiverId()).isEqualTo(2L);
        assertThat(request.getContent()).isEqualTo("Hello");
        assertThat(request.getItemId()).isEqualTo(3L);
    }

    @Test
    void noArgsConstructor_AndSetters_WorkCorrectly() {
        MessageCreateRequest request = new MessageCreateRequest();
        
        request.setReceiverId(2L);
        request.setContent("Hello World");
        request.setItemId(3L);
        
        assertThat(request.getReceiverId()).isEqualTo(2L);
        assertThat(request.getContent()).isEqualTo("Hello World");
        assertThat(request.getItemId()).isEqualTo(3L);
    }

    @Test
    void nullValues_AreAllowed() {
        MessageCreateRequest request = new MessageCreateRequest();
        
        assertThat(request.getReceiverId()).isNull();
        assertThat(request.getContent()).isNull();
        assertThat(request.getItemId()).isNull();
    }
}
