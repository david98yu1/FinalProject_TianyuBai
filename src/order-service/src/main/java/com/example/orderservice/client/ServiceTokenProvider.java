package com.example.orderservice.client;

import com.example.commonlib.dto.auth.AuthResponse;
import com.example.commonlib.dto.auth.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class ServiceTokenProvider {
    private final AuthFeignClient authClient;

    @Value("${service.user}") private String user;
    @Value("${service.pass}") private String pass;

    private final AtomicReference<String> tokenRef = new AtomicReference<>();
    private final AtomicReference<Instant> expRef = new AtomicReference<>(Instant.EPOCH);

    public String get() {
        // refresh if no token or expiring within 60s
        if (tokenRef.get() == null || isExpiringSoon()) {
            refresh();
        }
        return tokenRef.get();
    }

    public synchronized String refresh() {
        AuthResponse resp = authClient.login(new LoginRequest(user, pass));
        if (resp == null || resp.getToken() == null || resp.getToken().isBlank()) {
            throw new IllegalStateException("Auth login returned no token");
        }
        tokenRef.set(resp.getToken());
        // if expiresAt is null, fall back to 10 minutes from now
        expRef.set(resp.getExpiresAt() != null ? resp.getExpiresAt() : Instant.now().plusSeconds(600));
        return tokenRef.get();
    }

    private boolean isExpiringSoon() {
        Instant exp = expRef.get();
        return exp == null || Instant.now().plusSeconds(60).isAfter(exp);
    }
}
