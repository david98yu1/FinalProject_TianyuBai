package com.example.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String status;
    private BigDecimal amount;
    private String providerTxnId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
