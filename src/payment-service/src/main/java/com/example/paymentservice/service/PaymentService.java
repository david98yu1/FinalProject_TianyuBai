package com.example.paymentservice.service;

import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse pay(CreatePaymentRequest req);
    PaymentResponse get(Long id);
}
