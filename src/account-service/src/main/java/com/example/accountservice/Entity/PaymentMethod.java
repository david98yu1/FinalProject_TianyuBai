package com.example.accountservice.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pm_account"))
    private Account account;

    @Column(nullable = false, length = 20)
    private String brand;    // e.g., VISA, MASTERCARD

    @Column(nullable = false, length = 4)
    private String last4;    // "4242"

    @Column(nullable = false)
    private int expMonth;    // 1-12

    @Column(nullable = false)
    private int expYear;     // yyyy

    @Column(nullable = false, length = 128)
    private String token;    // token from payment gateway (never store PAN/CVV)

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}