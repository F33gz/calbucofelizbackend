package cl.metspherical.calbucofelizbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String username;
    private String avatar;
    private String description;
    private String names;
    private String lastNames;
    private List<String> roles;
}

