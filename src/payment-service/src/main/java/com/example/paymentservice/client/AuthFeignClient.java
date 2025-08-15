package com.example.paymentservice.client;

import com.example.commonlib.dto.auth.AuthResponse;
import com.example.commonlib.dto.auth.LoginRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "authClient", url = "${auth.service.base-url}")
public interface AuthFeignClient {
    @PostMapping(value = "/auth/login", consumes = "application/json")
    AuthResponse login(@RequestBody LoginRequest req);
}
