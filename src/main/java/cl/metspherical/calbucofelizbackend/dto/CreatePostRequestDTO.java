package cl.metspherical.calbucofelizbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequestDTO {

    private String username;

    private String content;

    private Set<String> categoryNames;

    private List<String> images;

}

