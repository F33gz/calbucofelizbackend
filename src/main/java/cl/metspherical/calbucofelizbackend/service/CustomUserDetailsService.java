package cl.metspherical.calbucofelizbackend.service;

import cl.metspherical.calbucofelizbackend.model.User;
import cl.metspherical.calbucofelizbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Custom implementation of UserDetailsService for Spring Security
 * Handles user authentication using RUT as username
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user details by RUT for Spring Security authentication
     *
     * @param rutAsString RUT as string (username for authentication)
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if user is not found or RUT is invalid
     */
    @Override
    public UserDetails loadUserByUsername(String rutAsString) throws UsernameNotFoundException {
        // Convert RUT from String to Integer
        String rut;
        try {
            rut = rutAsString;
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid RUT: " + rutAsString);
        }

        // Find user by RUT
        User user = userRepository.findByRut(rut)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with RUT: " + rut));

        // Convert to Spring Security UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getRut()) // Use RUT as username
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    /**
     * Converts user roles to Spring Security authorities
     *
     * @param user User entity containing roles
     * @return Collection of GrantedAuthority objects
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }
}