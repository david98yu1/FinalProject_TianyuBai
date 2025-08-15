package com.example.orderservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            try {
                Claims c = jwt.parse(token).getBody();
                String subject = c.getSubject(); // your user id / username

                // read roles from common claim names
                String rolesCsv = c.get("roles", String.class);
                if (rolesCsv == null) rolesCsv = c.get("authorities", String.class);
                if (rolesCsv == null) {
                    String scope = c.get("scope", String.class); // e.g. "PAYMENT ADMIN"
                    if (scope != null) rolesCsv = scope.replace(' ', ',');
                }

                List<String> rolesFromToken =
                        rolesCsv == null ? List.of()
                                : Arrays.stream(rolesCsv.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());

                // DEBUG: PRINT raw roles from token
                log.info("JWT user={} roles(from token)={}", subject, rolesFromToken);

                // Normalize for hasRole/hasAnyRole (ROLE_ prefix)
                List<GrantedAuthority> authorities =
                        rolesFromToken.stream()
                                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                // DEBUG: PRINT final authorities that Spring uses
                log.info("JWT user={} authorities(for Spring Security)={}",
                        subject,
                        authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));

                var auth = new UsernamePasswordAuthenticationToken(subject, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                log.warn("JWT parsing/validation failed: {}", e.getMessage());
                // leave unauthenticated
            }
        }
        chain.doFilter(req, res);
    }
}
