package tqs.backend.tqsbackend.dto;

import org.junit.jupiter.api.Test;
import tqs.backend.tqsbackend.entity.UserRoles;

import static org.assertj.core.api.Assertions.assertThat;

class UserRegistrationDtoTest {

    @Test
    void allArgsConstructorShouldPopulateFields() {
        UserRegistrationDto dto = new UserRegistrationDto("Alice", "alice@example.com", "secret123", UserRoles.RENTER);

        assertThat(dto.getName()).isEqualTo("Alice");
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getPassword()).isEqualTo("secret123");
        assertThat(dto.getRole()).isEqualTo(UserRoles.RENTER);
    }

    @Test
    void settersShouldAllowUpdates() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setName("Bob");
        dto.setEmail("bob@example.com");
        dto.setPassword("anotherSecret");
        dto.setRole(UserRoles.OWNER);

        assertThat(dto)
            .usingRecursiveComparison()
            .isEqualTo(new UserRegistrationDto("Bob", "bob@example.com", "anotherSecret", UserRoles.OWNER));
    }
}
