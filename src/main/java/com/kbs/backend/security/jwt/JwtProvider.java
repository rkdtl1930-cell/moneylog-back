package com.kbs.backend.security.jwt;

import com.kbs.backend.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

public interface JwtProvider {
    String generateToken(UserPrincipal userPrincipal);
    Authentication getAuthentication(HttpServletRequest request);
    boolean isTokenValid(HttpServletRequest request);
    String getTokenFromRequest(HttpServletRequest request);
}
