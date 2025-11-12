package com.ecom.dto.payment;

import com.ecom.entity.enums.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Value
@Builder
public class PaymentResponse {
    Long id;
    Long orderId;
    String stripePaymentIntentId;
    BigDecimal amount;
    String currency;
    PaymentStatus status;
    String paymentMethodId;
    String receiptUrl;
    String failureMessage;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
