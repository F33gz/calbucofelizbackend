package cl.metspherical.calbucofelizbackend.features.posts.dto;

import java.util.List;

public record PostPaginatedResponseDTO(
        List<PostDetailDTO> posts,
        Boolean hasNext,
        Integer currentPage,
        boolean isFirst,
        boolean isLast,
        boolean hasPreviuos
) {
}
