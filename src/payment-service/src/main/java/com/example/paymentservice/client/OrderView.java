package com.example.paymentservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderView {
    public Long id;
    public String status;
    public BigDecimal total;
}
