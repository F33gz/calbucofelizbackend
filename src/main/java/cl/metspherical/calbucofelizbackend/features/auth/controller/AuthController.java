package cl.metspherical.calbucofelizbackend.features.auth.controller;

import cl.metspherical.calbucofelizbackend.features.auth.dto.*;
import cl.metspherical.calbucofelizbackend.features.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO request) {
        try {
            Map<String, String> tokens = authenticationService.register(request);

            TokenDataDTO tokenData = new TokenDataDTO(
                    tokens.get("accessToken"),
                    tokens.get("refreshToken")
            );

            String username = tokens.get("username");

            AuthResponseDTO response = new AuthResponseDTO(username,tokenData);

            return ResponseEntity.ok(response);        } catch (Exception e) {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "error", 
                "Account already registered"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            Map<String, String> tokens = authenticationService.login(request);

            TokenDataDTO tokenData = new TokenDataDTO(
                    tokens.get("accessToken"),
                    tokens.get("refreshToken")
            );

            String username = tokens.get("username");

            AuthResponseDTO response = new AuthResponseDTO(username, tokenData);

            return ResponseEntity.ok(response);        } catch (Exception e) {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "error", 
                "Invalid RUT or password"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        try {
            Map<String, String> tokens = authenticationService.refreshToken(request.refreshToken());

            RefreshTokenResponseDTO response = new RefreshTokenResponseDTO(
                    tokens.get("accessToken")
            );

            return ResponseEntity.ok(response);        } catch (Exception e) {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                "error", 
                "Error refreshing token"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}