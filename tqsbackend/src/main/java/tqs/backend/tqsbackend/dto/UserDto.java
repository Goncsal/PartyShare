package tqs.backend.tqsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tqs.backend.tqsbackend.entity.UserRoles;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private UserRoles role;
    private boolean isActive;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserDto other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && Objects.equals(email, other.email)
                && Objects.equals(name, other.name)
                && role == other.role
                && isActive == other.isActive;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, role, isActive);
    }
}
