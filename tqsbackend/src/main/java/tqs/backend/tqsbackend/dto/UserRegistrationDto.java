package tqs.backend.tqsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tqs.backend.tqsbackend.entity.UserRoles;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDto {
    private String name;
    private String email;
    private String password;
    private UserRoles role = UserRoles.RENTER;
}
