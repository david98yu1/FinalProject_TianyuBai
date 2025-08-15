package com.example.paymentservice.client;

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

    @Value("${service.user}") private String user;   // e.g. payment-bot
    @Value("${service.pass}") private String pass;   // its password

    private final AtomicReference<String> tokenRef = new AtomicReference<>();
    private final AtomicReference<Instant> expRef   = new AtomicReference<>(Instant.EPOCH);

    /** Get a valid token (refreshes if missing/expiring soon). */
    public String get() {
        if (isExpiringSoon()) {
            refresh();
        }
        return tokenRef.get();
    }

    /** Force refresh by logging in to auth-service. */
    public synchronized String refresh() {
        AuthResponse resp = authClient.login(new LoginRequest(user, pass));
        if (resp == null || resp.getToken() == null || resp.getToken().isBlank()) {
            throw new IllegalStateException("Auth login returned no token");
        }
        tokenRef.set(resp.getToken());
        // if auth-service doesn’t send expiresAt, default to +10 minutes
        expRef.set(resp.getExpiresAt() != null ? resp.getExpiresAt() : Instant.now().plusSeconds(600));
        return tokenRef.get();
    }

    private boolean isExpiringSoon() {
        String t = tokenRef.get();
        Instant exp = expRef.get();
        return t == null || t.isBlank() || exp == null || Instant.now().plusSeconds(60).isAfter(exp);
    }
}
