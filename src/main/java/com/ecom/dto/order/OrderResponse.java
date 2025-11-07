package com.ecom.dto.order;

import com.ecom.dto.common.UserDto;
import com.ecom.entity.enums.OrderStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Value
@Builder
public class OrderResponse {
    Long id;
    BigDecimal total;
    OrderStatus status;
    String shippingAddress;
    OffsetDateTime placedAt;
    List<OrderItemResponse> items;
    UserDto user;
}

