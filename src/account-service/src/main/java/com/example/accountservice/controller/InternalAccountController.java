package com.example.accountservice.controller;

import com.example.accountservice.service.AccountService;
import com.example.commonlib.dto.account.CreateAccountOnRegister;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/accounts")
public class InternalAccountController {
    private final AccountService accountService;

    @PostMapping("/on-register")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_SYSTEM')")
    public void onRegister(@RequestBody CreateAccountOnRegister dto) {
        System.out.println("register request get");
        accountService.createIfAbsent(
                String.valueOf(dto.getAuthUserId()),
                dto.getEmail(),
                dto.getUsername()
        );
    }
}
