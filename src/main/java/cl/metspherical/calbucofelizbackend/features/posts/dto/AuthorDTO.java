package cl.metspherical.calbucofelizbackend.features.posts.dto;

import java.util.List;

public record AuthorDTO (
    String username,
    String avatar,
    List<String> roles
) {
}

