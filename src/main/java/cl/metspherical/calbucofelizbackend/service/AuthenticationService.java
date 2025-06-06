package cl.metspherical.calbucofelizbackend.service;

import cl.metspherical.calbucofelizbackend.dto.LoginRequestDTO;
import cl.metspherical.calbucofelizbackend.dto.RegisterRequestDTO;
import cl.metspherical.calbucofelizbackend.model.User;
import cl.metspherical.calbucofelizbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for handling user authentication operations
 * including registration, login, and token refresh functionality
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the system
     * 
     * @param request DTO containing user registration data
     * @return Map containing access token, refresh token and username
     */
    public Map<String, String> register(RegisterRequestDTO request) {
        // Build new user with encoded password
        User user = User.builder()
                .rut(request.rut())
                .names(request.names())
                .password(passwordEncoder.encode(request.password()))
                .number(Integer.valueOf(request.number()))
                .address(request.address())
                .username(request.names()+request.lastnames())
                .email(null)
                .avatar(null)
                .description(null)
                .lastNames(request.lastnames())
                .build();

        userRepository.save(user);

        // Generate authentication tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("username", user.getUsername());

        return tokens;
    }

    /**
     * Authenticates a user and generates new tokens
     * 
     * @param request DTO containing login credentials
     * @return Map containing access token, refresh token and username
     */
    public Map<String, String> login(LoginRequestDTO request) {
        // Authenticate user credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.rut(),
                        request.password()
                )
        );        
        
        // Find authenticated user
        User user = userRepository.findByRut(request.rut())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate new authentication tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("username", user.getUsername());

        return tokens;
    }

    /**
     * Refreshes an access token using a valid refresh token
     * 
     * @param refreshToken Valid refresh token string
     * @return Map containing new access token
     */
    public Map<String, String> refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // Extract user information from token
        String rut = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByRut((rut))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);

        return Map.of("accessToken", newAccessToken);
    }
}