package com.example.accountservice.service.impl;

import com.example.accountservice.dto.*;
import com.example.accountservice.entity.*;
import com.example.accountservice.mapper.AccountMapper;
import com.example.accountservice.repository.*;
import com.example.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepo;
    private final AddressRepository addressRepo;
    private final PaymentMethodRepository pmRepo;

    public AccountResponse createAccount(CreateAccountRequest req) {
        if (accountRepo.existsByEmail(req.email())) throw new IllegalArgumentException("email already exists");
        var acc = Account.builder().authUserId(req.authUserId()).email(req.email()).username(req.username()).build();
        return AccountMapper.toResponse(accountRepo.save(acc));
    }

    @Transactional(readOnly = true)
    public AccountResponse getById(Long id) {
        var acc = accountRepo.findById(id).orElseThrow(() -> new NoSuchElementException("account not found"));
        acc.getAddresses().size(); acc.getPaymentMethods().size(); // init lazies if needed
        return AccountMapper.toResponse(acc);
    }

    public AccountResponse update(Long id, UpdateAccountRequest req) {
        var acc = accountRepo.findById(id).orElseThrow(() -> new NoSuchElementException("account not found"));
        acc.setEmail(req.email()); acc.setUsername(req.username());
        return AccountMapper.toResponse(acc);
    }

    public java.util.List<AddressDto> addAddress(Long accountId, AddressRequest req) {
        var acc = accountRepo.findById(accountId).orElseThrow(() -> new NoSuchElementException("account not found"));
        var address = Address.builder()
                .account(acc).type(req.type()).line1(req.line1()).line2(req.line2())
                .city(req.city()).state(req.state()).zip(req.zip()).country(req.country())
                .isDefault(req.isDefault()).build();
        addressRepo.save(address);
        return addressRepo.findByAccountId(accountId).stream().map(AccountMapper::toDto).toList();
    }

    public java.util.List<PaymentMethodDto> addPaymentMethod(Long accountId, PaymentMethodRequest req) {
        var acc = accountRepo.findById(accountId).orElseThrow(() -> new NoSuchElementException("account not found"));
        var pm = PaymentMethod.builder()
                .account(acc).brand(req.brand()).last4(req.last4())
                .expMonth(req.expMonth()).expYear(req.expYear())
                .token(req.token()).isDefault(req.isDefault()).build();
        pmRepo.save(pm);
        return pmRepo.findByAccountId(accountId).stream().map(AccountMapper::toDto).toList();
    }
}
