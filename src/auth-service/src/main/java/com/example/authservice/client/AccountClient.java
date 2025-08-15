package com.example.authservice.client;

import com.example.commonlib.dto.account.CreateAccountOnRegister;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "accountClient",
        url = "${ACCOUNT_SERVICE_BASE_URL}",
        configuration = AccountFeignConfig.class
)
public interface AccountClient {
    @PostMapping("/internal/accounts/on-register")
    void createOnRegister(CreateAccountOnRegister req);
}
