package com.ecom.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentConfirmRequest {

    @NotBlank
    private String paymentIntentId;

    private String paymentMethodId;
}
