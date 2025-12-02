package tqs.backend.tqsbackend.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserLoginRequestTest {

    @Test
    void constructorShouldSetEmailAndPassword() {
        UserLoginRequest request = new UserLoginRequest("user@example.com", "plainPassword");

        assertThat(request.getEmail()).isEqualTo("user@example.com");
        assertThat(request.getPassword()).isEqualTo("plainPassword");
    }

    @Test
    void settersShouldUpdateValues() {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("newSecret");

        assertThat(request)
            .usingRecursiveComparison()
            .isEqualTo(new UserLoginRequest("alice@example.com", "newSecret"));
    }
}
