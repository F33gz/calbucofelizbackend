package cl.metspherical.calbucofelizbackend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostDetailDTO {
    private String content;
    private LocalDateTime createdAt;
    private AuthorDTO author;
    private List<PostImageDTO> images;
    private List<CategoryDTO> categories;
}

