package tqs.backend.tqsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tqs.backend.tqsbackend.entity.UserRoles;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private UserRoles role;
    private boolean isActive;
}
