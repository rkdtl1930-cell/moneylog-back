package com.kbs.backend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Log4j2
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtProvider jwtProvider;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        log.info("Authorization header: {}", authHeader);
        String token = jwtProvider.getTokenFromRequest(request);
        System.out.println("Token from header: " + token);
        if(token != null && !token.isEmpty()){
            try{
                Authentication authentication = jwtProvider.getAuthentication(request);
                if (authentication != null && jwtProvider.isTokenValid(request)) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }catch (Exception e){
                log.info("Invalid JWT token : {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
