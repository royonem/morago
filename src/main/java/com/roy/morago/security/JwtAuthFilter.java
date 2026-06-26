package com.roy.morago.security;

import com.roy.morago.entity.user.User;
import com.roy.morago.repository.user.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var claims = jwtProvider.getClaims(token);
                String username = claims.getSubject();
                User user = userRepository.findByEmail(username).orElse(null);

                java.util.List<?> rawRoles = claims.get("roleNames", java.util.List.class);
                java.util.List<String> roles = (rawRoles == null ? java.util.Collections.emptyList() : rawRoles)
                        .stream()
                        .map(Object::toString)
                        .toList();
                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                var auth = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (ExpiredJwtException e) {
                log.debug("JWT expired: {}", request.getRequestURI());
            } catch (SignatureException e) {
                log.debug("Invalid JWT signature: {}", request.getRequestURI());
            } catch (MalformedJwtException e) {
                log.debug("Malformed JWT: {}", request.getRequestURI());
            } catch (Exception e) {
                log.error("Unexpected JWT error: {}", request.getRequestURI(), e);
            }
        }
        filterChain.doFilter(request, response);
    }
}