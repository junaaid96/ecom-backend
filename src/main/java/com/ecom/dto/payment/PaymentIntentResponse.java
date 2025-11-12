package com.ecom.dto.payment;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PaymentIntentResponse {
    Long paymentId;
    String clientSecret;
    String paymentIntentId;
    BigDecimal amount;
    String currency;
    String status;
}
