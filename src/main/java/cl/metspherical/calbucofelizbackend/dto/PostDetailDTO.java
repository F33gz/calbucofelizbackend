package cl.metspherical.calbucofelizbackend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailDTO(
    String content,
    LocalDateTime createdAt,
    AuthorDTO author,
    List<PostImageDTO> images,
    List<CategoryDTO> categories
) {
}

