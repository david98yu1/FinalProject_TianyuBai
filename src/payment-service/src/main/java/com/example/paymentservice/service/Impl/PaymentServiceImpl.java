package com.example.paymentservice.service.Impl;

import com.example.paymentservice.client.OrderClient;
import com.example.paymentservice.client.OrderView;
import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.entity.*;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repo;
    private final OrderClient orderClient;

    @Override
    public PaymentResponse pay(CreatePaymentRequest req) {
        // 1) Read order to validate amount
        OrderView order = orderClient.getOrder(req.getOrderId());
        if (order == null) throw new IllegalArgumentException("order not found: " + req.getOrderId());

        // Compare amounts
        if (order.total == null || req.getAmount().compareTo(order.total) != 0) {
            throw new IllegalArgumentException("amount mismatch");
        }

        // 2) Create INITIATED payment
        Payment p = Payment.builder()
                .orderId(order.id)
                .amount(req.getAmount())
                .status(PaymentStatus.INITIATED)
                .build();
        p = repo.save(p);

        // 3) Simulate processor (deterministic demo rule):
        //    fail if amount (in cents) is divisible by 7; else succeed.
        boolean success = req.getAmount().movePointRight(2).remainder(BigDecimal.valueOf(7)).intValue() != 0;

        if (success) {
            p.setStatus(PaymentStatus.CAPTURED);
            p.setProviderTxnId("txn_" + UUID.randomUUID());
            // Optional: if your Order stays PENDING until payment, you would call a /confirm endpoint here.
            orderClient.confirmOrder(order.id);
            return toResp(repo.save(p));
        } else {
            p.setStatus(PaymentStatus.FAILED);
            repo.save(p);
            // Compensate: cancel the order (your cancel already restocks)
            orderClient.cancelOrder(order.id);
            return toResp(p);
        }
    }

    @Override @Transactional(readOnly = true)
    public PaymentResponse get(Long id) {
        Payment p = repo.findById(id).orElseThrow(() -> new java.util.NoSuchElementException("payment not found"));
        return toResp(p);
    }

    private PaymentResponse toResp(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getOrderId(), p.getStatus().name(), p.getAmount(),
                p.getProviderTxnId(), p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
