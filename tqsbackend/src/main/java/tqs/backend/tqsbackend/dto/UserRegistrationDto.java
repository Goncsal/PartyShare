package tqs.backend.tqsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tqs.backend.tqsbackend.entity.UserRoles;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDto {
    private String name;
    private String email;
    private String password;
    private UserRoles role;
}
