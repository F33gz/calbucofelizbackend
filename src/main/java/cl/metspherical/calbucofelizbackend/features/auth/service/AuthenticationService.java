package cl.metspherical.calbucofelizbackend.features.auth.service;

import cl.metspherical.calbucofelizbackend.common.security.service.JwtService;
import cl.metspherical.calbucofelizbackend.features.auth.dto.LoginRequestDTO;
import cl.metspherical.calbucofelizbackend.features.auth.dto.RegisterRequestDTO;
import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String REFRESH_TOKEN_KEY = "refreshToken";
    private static final String USERNAME_KEY = "username";


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

        userRepository.save(user);        // Generate authentication tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return createTokenResponse(accessToken, refreshToken, user.getUsername());
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));        // Generate new authentication tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return createTokenResponse(accessToken, refreshToken, user.getUsername());
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }

        // Extract user information from token
        UUID id = jwtService.extractUserId(refreshToken);
        User user = userRepository.getReferenceById((id));

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);

        return Map.of(ACCESS_TOKEN_KEY, newAccessToken);
    }

    /**
     * Creates a token response map with access token, refresh token and username
     */
    private Map<String, String> createTokenResponse(String accessToken, String refreshToken, String username) {
        Map<String, String> tokens = new HashMap<>();
        tokens.put(ACCESS_TOKEN_KEY, accessToken);
        tokens.put(REFRESH_TOKEN_KEY, refreshToken);
        tokens.put(USERNAME_KEY, username);
        return tokens;
    }
}