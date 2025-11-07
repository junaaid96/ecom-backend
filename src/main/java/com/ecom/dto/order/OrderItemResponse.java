package com.ecom.dto.order;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderItemResponse {
    Long id;
    Long productId;
    String productName;
    Integer quantity;
    BigDecimal unitPrice;
}

