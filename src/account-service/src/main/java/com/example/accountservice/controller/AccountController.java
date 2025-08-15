package com.example.accountservice.controller;

import com.example.accountservice.dto.*;
import com.example.accountservice.service.AccountService;
import com.example.commonlib.dto.account.CreateAccountOnRegister;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest req) {
        return service.createAccount(req);
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable Long id) { return service.getById(id); }

    @PutMapping("/{id}")
    public AccountResponse update(@PathVariable Long id, @Valid @RequestBody UpdateAccountRequest req) {
        return service.update(id, req);
    }

    @PostMapping("/{id}/addresses")
    public List<AddressDto> addAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest req) {
        System.out.println("add address request get");
        return service.addAddress(id, req);
    }

    @PostMapping("/{id}/payment-methods")
    public List<PaymentMethodDto> addPayment(@PathVariable Long id, @Valid @RequestBody PaymentMethodRequest req) {
        return service.addPaymentMethod(id, req);
    }
}
