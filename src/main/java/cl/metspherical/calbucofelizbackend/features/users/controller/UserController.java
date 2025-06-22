package cl.metspherical.calbucofelizbackend.features.users.controller;

import cl.metspherical.calbucofelizbackend.features.users.dto.UserProfileDTO;
import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import cl.metspherical.calbucofelizbackend.features.users.dto.UserSearchResponseDTO;
import cl.metspherical.calbucofelizbackend.features.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserProfileDTO userProfile = new UserProfileDTO(
                user.getUsername(),
                user.getAvatar(),
                user.getDescription(),
                user.getNames(),
                user.getLastNames(),
                user.getRoles()
        );
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping()
    public ResponseEntity<UserSearchResponseDTO> getUsers(@RequestParam String username) {
        UserSearchResponseDTO searchResult = userService.searchUsers(username);
        return ResponseEntity.ok(searchResult);
    }
}

