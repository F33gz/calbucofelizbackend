package cl.metspherical.calbucofelizbackend.common.security.utils;

import cl.metspherical.calbucofelizbackend.common.security.jwt.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

public class SecurityUtils {

    public static UUID getCurrentUserId() {
        return getCurrentAuth().userId();
    }

    public static List<String> getCurrentUserRoles() {
        return getCurrentAuth().roles();
    }

    public static boolean hasRole(String role) {
        return getCurrentAuth().roles().contains(role);
    }

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth instanceof JwtAuthenticationToken;
    }

    private static JwtAuthenticationToken getCurrentAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (JwtAuthenticationToken) auth;
    }
}
