package cl.metspherical.calbucofelizbackend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostDetailDTO(
        UUID id,
        String content,
        LocalDateTime createdAt,
        AuthorDTO author,
        List<String> images,
        List<CategoryDTO> categories,
        Integer likes,
        Integer comments
) { }

