package com.example.itemservice.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
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
                String subject = c.getSubject(); // we'll treat this as userId
                String rolesCsv = c.get("roles", String.class);
                var auths = rolesCsv == null ? java.util.List.<SimpleGrantedAuthority>of()
                        : Arrays.stream(rolesCsv.split(",")).map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toList());

                var auth = new UsernamePasswordAuthenticationToken(subject, null, auths);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) { /* leave unauthenticated */ }
        }
        chain.doFilter(req, res);
    }
}
