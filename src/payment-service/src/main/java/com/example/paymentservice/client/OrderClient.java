package com.example.paymentservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderClient {
    private final WebClient orderClient;

    public OrderView getOrder(Long id) {
        return orderClient.get().uri("/orders/{id}", id)
                .retrieve()
                .bodyToMono(OrderView.class)
                .block();
    }

    public void cancelOrder(Long id) {
        orderClient.post()
                .uri("/orders/{id}/cancel", id)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        rsp -> rsp.bodyToMono(String.class)
                                .defaultIfEmpty("bad request")
                                .map(msg -> new IllegalArgumentException("cancel failed: " + msg)) // <- return Mono<Throwable>
                )
                .toBodilessEntity()
                .block();

    }

    public void confirmOrder(Long id) {
        orderClient.post().uri("/orders/{id}/confirm", id).retrieve().onStatus(
                        HttpStatusCode::isError,
                        rsp -> rsp.bodyToMono(String.class)
                                .defaultIfEmpty("order-service error")
                                .map(msg -> new IllegalStateException("Confirm order failed: " + msg))
                )
                .toBodilessEntity()
                .block();
    }
}
