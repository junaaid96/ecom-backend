package com.ecom.service;

import com.ecom.dto.order.OrderResponse;
import com.ecom.dto.product.ProductResponse;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, Object> getDashboardSummary();
    List<ProductResponse> getTopProducts(int limit);
    List<OrderResponse> getRecentOrders(int limit);
    Map<String, Object> getSalesAnalytics(int days);
    List<ProductResponse> getOutOfStockProducts();
}

