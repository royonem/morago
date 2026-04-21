package com.roy.morago.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
                var roles = rawRoles.stream()
                        .map(Object::toString)
                        .toList();
                var authorities = roles.stream()
                        .map(role -> "ROLE_" + role)
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .toList();
                var auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                logger.debug("Invalid JWT");
            }
        }
        filterChain.doFilter(request, response);
    }
}