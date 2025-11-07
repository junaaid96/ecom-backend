package com.ecom.service;

import com.ecom.dto.order.OrderRequest;
import com.ecom.dto.order.OrderResponse;
import com.ecom.entity.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request);

    List<OrderResponse> getOrdersForCurrentUser();

    List<OrderResponse> getAllOrders();

    OrderResponse getOrder(Long id);

    OrderResponse updateOrderStatus(Long id, OrderStatus status);
}

