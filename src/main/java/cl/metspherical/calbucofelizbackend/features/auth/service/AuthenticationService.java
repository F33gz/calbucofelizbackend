package cl.metspherical.calbucofelizbackend.features.auth.service;

import cl.metspherical.calbucofelizbackend.common.security.service.JwtService;
import cl.metspherical.calbucofelizbackend.features.auth.dto.LoginRequestDTO;
import cl.metspherical.calbucofelizbackend.features.auth.dto.RegisterRequestDTO;
import cl.metspherical.calbucofelizbackend.features.auth.dto.RecoveryRequestDTO;
import cl.metspherical.calbucofelizbackend.features.auth.utils.RutValidator;
import cl.metspherical.calbucofelizbackend.features.auth.utils.PhoneValidator;
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
import java.security.SecureRandom;

/**
 * Service responsible for handling user authentication operations
 * including registration, login, token refresh, and password recovery functionality
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TwilioService twilioService;
    private static final String ACCESS_TOKEN_KEY = "accessToken";
    private static final String REFRESH_TOKEN_KEY = "refreshToken";
    private static final String USERNAME_KEY = "username";


    /**
     * Registers a new user in the system with RUT and phone number validation
     * 
     * @param request DTO containing user registration data
     * @return Map containing access token, refresh token and username
     * @throws ResponseStatusException if validation fails
     */
    public Map<String, String> register(RegisterRequestDTO request) {
        // Validate RUT format and check digit
        if (!RutValidator.validateRut(request.rut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid RUT format. Please check format and verification digit.");
        }

        // Validate Chilean phone number
        if (!PhoneValidator.isValidChileanPhone(request.number())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid Chilean phone number format.");
        }

        // Check if RUT already exists
        if (userRepository.findByRut(request.rut()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A user with this RUT is already registered.");
        }

        // Build new user with encoded password and validated data
        User user = User.builder()
                .rut(request.rut()) // RUT already validated for format
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        // Generate new authentication tokens
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
     * Handles password recovery by validating user data and sending temporary password via SMS
     *
     * @param request DTO containing RUT and phone number for recovery
     * @throws ResponseStatusException if validation fails or SMS sending fails
     */
    public void recoverPassword(RecoveryRequestDTO request) {
        try {

            // Find user by RUT
            User user = userRepository.findByRut(request.rut()).orElse(null);

            // Validate that phone number matches the user with given RUT
            if (user == null || !user.getNumber().toString().equals(request.phone())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Password recovery attempt with invalid credentials RUT:"+request.phone()+" ,phone:"+request.phone());
            }

            // Generate secure 8-digit temporary password
            String temporaryPassword = generateTemporaryPassword();

            // Send SMS with temporary password
            twilioService.sendPasswordRecoverySms("+56"+request.phone(), temporaryPassword);

            user.setPassword(passwordEncoder.encode(temporaryPassword));
            userRepository.save(user);

        } catch (ResponseStatusException  e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error during password recovery for RUT"+request.rut()+e.getMessage());
        }
    }

    /**
     * Generates a secure random 8-digit temporary password
     *
     * @return String containing 8-digit password
     */
    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        int password = 10000000 + random.nextInt(90000000); // Ensures 8 digits
        return String.valueOf(password);
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