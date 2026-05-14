package com.roy.morago.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtAuthFilter.class);

    public JwtAuthFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var claims = jwtProvider.getClaims(token);

                String username = claims.getSubject();
                java.util.List<?> rawRoles = claims.get("roleNames", java.util.List.class);
                java.util.List<String> roles = (rawRoles == null ? java.util.Collections.emptyList() : rawRoles)
                        .stream()
                        .map(Object::toString)
                        .toList();
                var authorities = roles.stream()
                        .map(role -> "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                var auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException e) {
                logger.debug("JWT expired: {}", request.getRequestURI());
            } catch (SignatureException e) {
                logger.debug("Invalid JWT signature: {}", request.getRequestURI());
            } catch (MalformedJwtException e) {
                logger.debug("Malformed JWT: {}", request.getRequestURI());
            } catch (Exception e) {
                logger.error("Unexpected JWT error: {}", request.getRequestURI(), e);
            }
        }
        filterChain.doFilter(request, response);
    }
}