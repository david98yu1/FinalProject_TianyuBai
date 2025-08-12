package com.example.accountservice.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(nullable = false, length = 50)
    private String username;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    public void addAddress(Address a) {
        a.setAccount(this);
        this.addresses.add(a);
    }
    public void removeAddress(Address a) {
        a.setAccount(null);
        this.addresses.remove(a);
    }
    public void addPaymentMethod(PaymentMethod pm) {
        pm.setAccount(this);
        this.paymentMethods.add(pm);
    }
    public void removePaymentMethod(PaymentMethod pm) {
        pm.setAccount(null);
        this.paymentMethods.remove(pm);
    }
}