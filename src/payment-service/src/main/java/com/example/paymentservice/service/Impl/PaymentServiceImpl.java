package com.example.paymentservice.service.Impl;

import com.example.paymentservice.client.OrderFeignClient;
import com.example.paymentservice.dto.CreatePaymentRequest;
import com.example.paymentservice.dto.PaymentResponse;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.entity.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.PaymentService;
import com.example.commonlib.dto.order.OrderResponse; // use your local DTO (or swap to common-lib if you moved it)
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
    private final OrderFeignClient orderClient;

    @Override
    public PaymentResponse pay(CreatePaymentRequest req) {
        // Read order to validate amount
        OrderResponse order = orderClient.get(req.getOrderId());
        if (order == null) throw new IllegalArgumentException("order not found: " + req.getOrderId());

        // Compare amounts
        if (order.getTotal() == null || req.getAmount().compareTo(order.getTotal()) != 0) {
            throw new IllegalArgumentException("amount mismatch");
        }

        // Create INITIATED payment
        Payment p = Payment.builder()
                .orderId(order.getId())
                .amount(req.getAmount())
                .status(PaymentStatus.INITIATED)
                .build();
        p = repo.save(p);

        // Simulate processor (deterministic demo rule):
        // fail if amount (in cents) is divisible by 7; else succeed.
        boolean success = req.getAmount().movePointRight(2).remainder(BigDecimal.valueOf(7)).intValue() != 0;

        if (success) {
            p.setStatus(PaymentStatus.CAPTURED);
            p.setProviderTxnId("txn_" + UUID.randomUUID());
            orderClient.confirm(order.getId());
            return toResp(repo.save(p));
        } else {
            p.setStatus(PaymentStatus.FAILED);
            repo.save(p);
            orderClient.confirm(order.getId());
            return toResp(p);
        }
    }

    @Override
    @Transactional(readOnly = true)
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
