package com.ecom.mapper;

import com.ecom.dto.order.OrderItemResponse;
import com.ecom.dto.order.OrderResponse;
import com.ecom.entity.Order;
import com.ecom.entity.OrderItem;

import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(OrderMapper::toItemResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .total(order.getTotal())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .placedAt(order.getPlacedAt())
                .items(itemResponses)
                .user(UserMapper.toUserDto(order.getUser()))
                .build();
    }

    public static OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build();
    }
}

