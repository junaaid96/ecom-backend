package com.ecom.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentIntentRequest {

    @NotNull
    private Long orderId;
}
