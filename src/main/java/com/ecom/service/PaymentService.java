package com.ecom.service;

import com.ecom.dto.payment.PaymentIntentRequest;
import com.ecom.dto.payment.PaymentIntentResponse;
import com.ecom.dto.payment.PaymentResponse;

public interface PaymentService {

    PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request);

    PaymentResponse getPaymentByOrderId(Long orderId);

    PaymentResponse getPaymentById(Long id);

    void handleWebhook(String payload, String sigHeader);
}
