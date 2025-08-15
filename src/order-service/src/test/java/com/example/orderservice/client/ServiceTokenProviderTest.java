package com.example.orderservice.client;

import com.example.commonlib.dto.auth.AuthResponse;
import com.example.commonlib.dto.auth.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServiceTokenProviderTest {

    @Test
    @DisplayName("get(): caches token until near expiry; refresh(): forces login")
    void token_cache_and_refresh() {
        AuthFeignClient auth = mock(AuthFeignClient.class);
        ServiceTokenProvider stp = new ServiceTokenProvider(auth);
        ReflectionTestUtils.setField(stp, "user", "svc");
        ReflectionTestUtils.setField(stp, "pass", "pass");

        // First login
        when(auth.login(any(LoginRequest.class))).thenAnswer(inv -> {
            LoginRequest lr = inv.getArgument(0);
            return new AuthResponse("t1", Instant.now().plusSeconds(600));
        });

        String t1 = stp.get();
        assertThat(t1).isEqualTo("t1");
        verify(auth, times(1)).login(any(LoginRequest.class));

        // Second get() before expiry -> returns cached, no new login
        String t2 = stp.get();
        assertThat(t2).isEqualTo("t1");
        verifyNoMoreInteractions(auth);

        // Force refresh -> new token
        when(auth.login(any(LoginRequest.class))).thenReturn(new AuthResponse("t2", Instant.now().plusSeconds(600)));
        String t3 = stp.refresh();
        assertThat(t3).isEqualTo("t2");
        verify(auth, times(2)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("get(): if server returns no token -> throws")
    void get_throws_when_no_token() {
        AuthFeignClient auth = mock(AuthFeignClient.class);
        ServiceTokenProvider stp = new ServiceTokenProvider(auth);
        ReflectionTestUtils.setField(stp, "user", "svc");
        ReflectionTestUtils.setField(stp, "pass", "pass");

        when(auth.login(any(LoginRequest.class))).thenReturn(new AuthResponse(null, Instant.now().plusSeconds(600)));
        assertThatThrownBy(stp::get).isInstanceOf(IllegalStateException.class);
    }
}