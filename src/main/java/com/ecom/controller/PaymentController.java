package com.ecom.controller;

import com.ecom.dto.payment.PaymentIntentRequest;
import com.ecom.dto.payment.PaymentIntentResponse;
import com.ecom.dto.payment.PaymentResponse;
import com.ecom.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody PaymentIntentRequest request
    ) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(request));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}
