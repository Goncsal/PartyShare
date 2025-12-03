package tqs.backend.tqsbackend.dto;

import org.junit.jupiter.api.Test;
import tqs.backend.tqsbackend.entity.UserRoles;

import static org.assertj.core.api.Assertions.assertThat;

class UserDtoTest {

    @Test
    void constructorShouldPopulateAllFields() {
        UserDto dto = new UserDto(1L, "Alice", "alice@example.com", UserRoles.RENTER, true);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Alice");
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getRole()).isEqualTo(UserRoles.RENTER);
        assertThat(dto.isActive()).isTrue();
    }

    @Test
    void settersShouldUpdateState() {
        UserDto dto = new UserDto();
        dto.setId(2L);
        dto.setName("Bob");
        dto.setEmail("bob@example.com");
        dto.setRole(UserRoles.OWNER);
        dto.setActive(false);

        assertThat(dto)
            .usingRecursiveComparison()
            .isEqualTo(new UserDto(2L, "Bob", "bob@example.com", UserRoles.OWNER, false));
    }

    @Test
    void equalsAndHashCodeShouldUseAllFields() {
        UserDto first = new UserDto(3L, "Carol", "carol@example.com", UserRoles.RENTER, true);
        UserDto second = new UserDto(3L, "Carol", "carol@example.com", UserRoles.RENTER, true);
        UserDto differentEmail = new UserDto(3L, "Carol", "other@example.com", UserRoles.RENTER, true);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(differentEmail);
    }
}
