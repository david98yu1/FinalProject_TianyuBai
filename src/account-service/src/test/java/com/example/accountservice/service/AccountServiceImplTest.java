package com.example.accountservice.service;

import com.example.accountservice.dto.*;
import com.example.accountservice.entity.Account;
import com.example.accountservice.entity.Address;
import com.example.accountservice.entity.PaymentMethod;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.repository.AddressRepository;
import com.example.accountservice.repository.PaymentMethodRepository;
import com.example.accountservice.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock AccountRepository accountRepo;
    @Mock AddressRepository addressRepo;
    @Mock PaymentMethodRepository pmRepo;

    // We don't @InjectMocks because AccountServiceImpl has a lombok-required-args constructor.
    // We'll new it up in @BeforeEach to be explicit about the constructor being used.
    AccountServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AccountServiceImpl(accountRepo, addressRepo, pmRepo);
    }

    @Test
    @DisplayName("createAccount: saves when email not taken")
    void createAccount_success() {
        CreateAccountRequest req = new CreateAccountRequest("a@b.com", "alice", "u-1");
        when(accountRepo.existsByEmail("a@b.com")).thenReturn(false);
        Account saved = Account.builder().id(1L).authUserId("u-1").email("a@b.com").username("alice").build();
        when(accountRepo.save(any(Account.class))).thenReturn(saved);

        AccountResponse out = service.createAccount(req);

        // verify save got the right values
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepo).save(captor.capture());
        Account toSave = captor.getValue();
        assertThat(toSave.getAuthUserId()).isEqualTo("u-1");
        assertThat(toSave.getEmail()).isEqualTo("a@b.com");
        assertThat(toSave.getUsername()).isEqualTo("alice");

        // verify mapping
        assertThat(out.getId()).isEqualTo(1L);
        assertThat(out.getAuthUserId()).isEqualTo("u-1");
        assertThat(out.getEmail()).isEqualTo("a@b.com");
        assertThat(out.getUsername()).isEqualTo("alice");
    }

    @Test
    @DisplayName("createAccount: throws if email already exists")
    void createAccount_emailExists() {
        when(accountRepo.existsByEmail("dup@x.com")).thenReturn(true);
        CreateAccountRequest req = new CreateAccountRequest("dup@x.com", "dup", "u-2");

        assertThatThrownBy(() -> service.createAccount(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
        verify(accountRepo, never()).save(any());
    }

    @Test
    @DisplayName("getById: returns mapped DTO when found")
    void getById_found() {
        Account acc = Account.builder().id(10L).authUserId("u-10").email("e@x.com").username("eve").build();
        // avoid NPE on lazy collection init call in code
        acc.setAddresses(new java.util.ArrayList<>());
        acc.setPaymentMethods(new java.util.ArrayList<>());
        when(accountRepo.findById(10L)).thenReturn(Optional.of(acc));

        AccountResponse out = service.getById(10L);

        assertThat(out.getId()).isEqualTo(10L);
        assertThat(out.getAuthUserId()).isEqualTo("u-10");
        assertThat(out.getEmail()).isEqualTo("e@x.com");
        assertThat(out.getUsername()).isEqualTo("eve");
    }

    @Test
    @DisplayName("getById: throws when not found")
    void getById_notFound() {
        when(accountRepo.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(404L))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessageContaining("account not found");
    }

    @Test
    @DisplayName("addAddress: looks up account, saves address, returns mapped list")
    void addAddress_success() {
        Long accountId = 1L;
        Account acc = Account.builder().id(accountId).authUserId("u-1").email("a@b.com").username("alice").build();
        // In the current implementation the service uses findByAuthUserId(...) for the lookup in addAddress.
        when(accountRepo.findByAuthUserId(anyString())).thenReturn(Optional.of(acc));

        AddressRequest req = new AddressRequest(
                com.example.accountservice.entity.AddressType.SHIPPING,
                "123 Main", null, "LA", "CA", "90001", "US", true
        );

        // return whatever list the repo gives; mapping content is not strongly asserted here
        Address a1 = Address.builder().id(100L).account(acc).type(req.getType())
                .line1(req.getLine1()).line2(req.getLine2()).city(req.getCity()).state(req.getState())
                .zip(req.getZip()).country(req.getCountry()).isDefault(req.isDefault()).build();
        when(addressRepo.save(any(Address.class))).thenReturn(a1);
        when(addressRepo.findByAccountId(accountId)).thenReturn(List.of(a1));

        List<AddressDto> out = service.addAddress(accountId, req);

        // verify we looked up by auth user id and then fetched by numeric account id for the response list
        verify(accountRepo).findByAuthUserId(anyString());
        verify(addressRepo).findByAccountId(accountId);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("addPaymentMethod: saves and returns mapped list")
    void addPaymentMethod_success() {
        Long accountId = 2L;
        Account acc = Account.builder().id(accountId).authUserId("u-2").email("b@c.com").username("bob").build();
        when(accountRepo.findById(accountId)).thenReturn(Optional.of(acc));

        PaymentMethodRequest req = new PaymentMethodRequest("VISA", "4242", 12, 2030, "tok_abc", true);
        PaymentMethod pm = PaymentMethod.builder().id(200L).account(acc).brand(req.getBrand())
                .last4(req.getLast4()).expMonth(req.getExpMonth()).expYear(req.getExpYear())
                .token(req.getToken()).isDefault(req.isDefault()).build();

        when(pmRepo.save(any(PaymentMethod.class))).thenReturn(pm);
        when(pmRepo.findByAccountId(accountId)).thenReturn(List.of(pm));

        List<PaymentMethodDto> out = service.addPaymentMethod(accountId, req);

        verify(pmRepo).save(any(PaymentMethod.class));
        verify(pmRepo).findByAccountId(accountId);
        assertThat(out).hasSize(1);
        assertThat(out.get(0).getId()).isEqualTo(200L);
        assertThat(out.get(0).getBrand()).isEqualTo("VISA");
    }

    @Test
    @DisplayName("createIfAbsent: returns existing when found")
    void createIfAbsent_existing() {
        Account existing = Account.builder().id(9L).authUserId("u-9").email("x@x.com").username("x").build();
        when(accountRepo.findByAuthUserId("u-9")).thenReturn(Optional.of(existing));

        AccountResponse out = service.createIfAbsent("u-9", "ignored@x.com", "ignored");
        verify(accountRepo, never()).save(any());
        assertThat(out.getId()).isEqualTo(9L);
        assertThat(out.getAuthUserId()).isEqualTo("u-9");
    }

    @Test
    @DisplayName("createIfAbsent: creates when missing and email free")
    void createIfAbsent_creates() {
        when(accountRepo.findByAuthUserId("u-10")).thenReturn(Optional.empty());
        when(accountRepo.existsByEmail("new@x.com")).thenReturn(false);
        Account saved = Account.builder().id(10L).authUserId("u-10").email("new@x.com").username("new").build();
        when(accountRepo.save(any(Account.class))).thenReturn(saved);

        AccountResponse out = service.createIfAbsent("u-10", "new@x.com", "new");

        verify(accountRepo).save(any(Account.class));
        assertThat(out.getId()).isEqualTo(10L);
        assertThat(out.getEmail()).isEqualTo("new@x.com");
    }

    @Test
    @DisplayName("createIfAbsent: throws if email already exists when creating")
    void createIfAbsent_emailTaken() {
        when(accountRepo.findByAuthUserId("u-11")).thenReturn(Optional.empty());
        when(accountRepo.existsByEmail("dup@x.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createIfAbsent("u-11", "dup@x.com", "dup"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(accountRepo, never()).save(any());
    }

    @Test
    @DisplayName("findByAuthUserId: delegates to repository")
    void findByAuthUserId_delegates() {
        when(accountRepo.findByAuthUserId("u-z")).thenReturn(Optional.empty());
        assertThat(service.findByAuthUserId("u-z")).isEmpty();
        verify(accountRepo).findByAuthUserId("u-z");
    }
}