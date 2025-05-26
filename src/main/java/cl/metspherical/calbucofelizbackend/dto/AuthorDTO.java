package cl.metspherical.calbucofelizbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorDTO {
    private String username;
    private String avatar;
}

