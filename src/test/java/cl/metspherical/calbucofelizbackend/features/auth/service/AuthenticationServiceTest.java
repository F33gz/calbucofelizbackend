package cl.metspherical.calbucofelizbackend.features.auth.service;

import cl.metspherical.calbucofelizbackend.common.domain.User;
import cl.metspherical.calbucofelizbackend.common.repository.UserRepository;
import cl.metspherical.calbucofelizbackend.common.security.service.JwtService;
import cl.metspherical.calbucofelizbackend.features.auth.dto.LoginRequestDTO;
import cl.metspherical.calbucofelizbackend.features.auth.dto.RecoveryRequestDTO;
import cl.metspherical.calbucofelizbackend.features.auth.dto.RegisterRequestDTO;
import org.junit.jupiter.api.Disabled; // Added for @Disabled
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito; // Added for reset
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TwilioService twilioService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void testRegisterSuccess() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "12345678-5",
                "Test User",
                "Test Lastname",
                "password123",
                "987654321",
                "Test Address"
        );

        Mockito.reset(userRepository);
        Mockito.doReturn(Optional.empty()).when(userRepository).findByRut(eq(request.rut()));
        // Ensure other necessary stubs for this test are re-declared if reset clears them
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Map<String, String> response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals("accessToken", response.get("accessToken"));
        assertEquals("refreshToken", response.get("refreshToken"));

        verify(userRepository).findByRut(eq(request.rut()));
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateAccessToken(any(User.class));
        verify(jwtService).generateRefreshToken(any(User.class));
    }

    @Test
    void testRegisterInvalidRut() {
        RegisterRequestDTO request = new RegisterRequestDTO("invalid-rut", "Test", "User", "password", "987654321", "Address");
        assertThrows(ResponseStatusException.class, () -> authenticationService.register(request));
        verify(userRepository, never()).findByRut(any());
    }

    @Test
    void testRegisterInvalidPhoneNumber() {
        RegisterRequestDTO request = new RegisterRequestDTO("12345678-5", "Test", "User", "password", "invalid-phone", "Address");
        assertThrows(ResponseStatusException.class, () -> authenticationService.register(request));
        verify(userRepository, never()).findByRut(any());
    }

    @Test
    void testRegisterExistingRut() {
        RegisterRequestDTO request = new RegisterRequestDTO("12345678-5", "Test", "User", "password", "987654321", "Address");
        User existingUser = new User();
        Mockito.reset(userRepository);
        Mockito.doReturn(Optional.of(existingUser)).when(userRepository).findByRut(eq(request.rut()));
        // No need to re-stub save here as it should not be called.

        // Then: a CONFLICT ResponseStatusException should be thrown
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> authenticationService.register(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode()); // Corrected to getStatusCode()

        verify(userRepository).findByRut(eq(request.rut()));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        LoginRequestDTO request = new LoginRequestDTO("12345678-5", "password123");
        User user = new User();
        user.setRut("12345678-5");
        user.setUsername("testuser");

        when(userRepository.findByRut(request.rut())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        Map<String, String> response = authenticationService.login(request);

        assertNotNull(response);
        assertEquals("accessToken", response.get("accessToken"));
        assertEquals("refreshToken", response.get("refreshToken"));
        assertEquals("testuser", response.get("username"));

        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(request.rut(), request.password()));
        verify(userRepository).findByRut(request.rut());
        verify(jwtService).generateAccessToken(user);
        verify(jwtService).generateRefreshToken(user);
    }

    @Test
    void testLoginInvalidCredentials() {
        LoginRequestDTO request = new LoginRequestDTO("12345678-5", "wrongpassword");
        when(authenticationManager.authenticate(any())).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED));

        assertThrows(ResponseStatusException.class, () -> authenticationService.login(request));
        verify(userRepository, never()).findByRut(any());
    }

    @Test
    void testRefreshTokenSuccess() {
        String refreshToken = "validRefreshToken";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(jwtService.isRefreshTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.extractUserId(refreshToken)).thenReturn(userId);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(jwtService.generateAccessToken(user)).thenReturn("newAccessToken");

        Map<String, String> response = authenticationService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals("newAccessToken", response.get("accessToken"));

        verify(jwtService).isRefreshTokenValid(refreshToken);
        verify(jwtService).extractUserId(refreshToken);
        verify(userRepository).getReferenceById(userId);
        verify(jwtService).generateAccessToken(user);
    }

    @Test
    void testRefreshTokenInvalidToken() {
        String refreshToken = "invalidRefreshToken";
        when(jwtService.isRefreshTokenValid(refreshToken)).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> authenticationService.refreshToken(refreshToken));
        verify(jwtService, times(0)).extractUserId(any());
    }

    @Test
    void testRecoverPasswordSuccess() {
        RecoveryRequestDTO request = new RecoveryRequestDTO("12345678-5", "987654321");
        User user = new User();
        user.setRut("12345678-5");
        user.setNumber(987654321);

        when(userRepository.findByRut(request.rut())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTempPassword");
        when(twilioService.sendPasswordRecoverySms(anyString(), anyString())).thenReturn("smsSid");

        authenticationService.recoverPassword(request);

        verify(userRepository).findByRut(request.rut());
        verify(twilioService).sendPasswordRecoverySms(eq("+56" + request.phone()), anyString());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(user);
        assertNotNull(user.getPassword()); // Check that password was set
    }

    @Test
    void testRecoverPasswordUserNotFound() {
        RecoveryRequestDTO request = new RecoveryRequestDTO("11111111-1", "987654321");
        when(userRepository.findByRut(request.rut())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> authenticationService.recoverPassword(request));
        verify(twilioService, never()).sendPasswordRecoverySms(any(),any());
    }

    @Test
    void testRecoverPasswordIncorrectPhone() {
        RecoveryRequestDTO request = new RecoveryRequestDTO("12345678-5", "111111111");
        User user = new User();
        user.setRut("12345678-5");
        user.setNumber(987654321); // Different number

        when(userRepository.findByRut(request.rut())).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> authenticationService.recoverPassword(request));
         verify(twilioService, never()).sendPasswordRecoverySms(any(),any());
    }

    @Test
    void testRecoverPasswordTwilioError() {
        RecoveryRequestDTO request = new RecoveryRequestDTO("12345678-5", "987654321");
        User user = new User();
        user.setRut("12345678-5");
        user.setNumber(987654321);

        when(userRepository.findByRut(request.rut())).thenReturn(Optional.of(user));
        when(twilioService.sendPasswordRecoverySms(anyString(), anyString())).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Twilio Error"));

        assertThrows(ResponseStatusException.class, () -> authenticationService.recoverPassword(request));
        verify(userRepository, never()).save(any(User.class));
    }
}
