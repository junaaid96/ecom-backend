package com.ecom.service.impl;

import com.ecom.dto.order.OrderResponse;
import com.ecom.dto.product.ProductResponse;
import com.ecom.entity.Order;
import com.ecom.entity.OrderItem;
import com.ecom.entity.Product;
import com.ecom.entity.enums.OrderStatus;
import com.ecom.mapper.OrderMapper;
import com.ecom.mapper.ProductMapper;
import com.ecom.repository.CategoryRepository;
import com.ecom.repository.OrderRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;
import com.ecom.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        long productCount = productRepository.count();
        long categoryCount = categoryRepository.count();
        long orderCount = orderRepository.count();
        long userCount = userRepository.count();
        
        // Calculate total revenue from all delivered orders
        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate pending orders count
        long pendingOrders = orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.PENDING)
                .count();
        
        // Calculate low stock products (stock < 10)
        long lowStockProducts = productRepository.findAll().stream()
                .filter(product -> product.getStock() < 10)
                .count();
        
        summary.put("products", productCount);
        summary.put("categories", categoryCount);
        summary.put("orders", orderCount);
        summary.put("users", userCount);
        summary.put("revenue", totalRevenue);
        summary.put("pendingOrders", pendingOrders);
        summary.put("lowStockProducts", lowStockProducts);
        
        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getTopProducts(int limit) {
        // Get products ordered by total quantity sold
        List<Order> allOrders = orderRepository.findAll();
        Map<Long, Integer> productSales = new HashMap<>();
        
        for (Order order : allOrders) {
            for (OrderItem item : order.getItems()) {
                productSales.merge(
                    item.getProduct().getId(),
                    item.getQuantity(),
                    Integer::sum
                );
            }
        }
        
        // Sort by sales quantity and get top products
        return productSales.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(entry -> {
                    Product product = productRepository.findById(entry.getKey())
                            .orElse(null);
                    return product != null ? ProductMapper.toResponse(product) : null;
                })
                .filter(product -> product != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getRecentOrders(int limit) {
        return orderRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "placedAt"))
        ).getContent().stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSalesAnalytics(int days) {
        Map<String, Object> analytics = new HashMap<>();
        
        OffsetDateTime startDate = OffsetDateTime.now().minusDays(days);
        
        List<Order> recentOrders = orderRepository.findAll().stream()
                .filter(order -> order.getPlacedAt().isAfter(startDate))
                .collect(Collectors.toList());
        
        BigDecimal totalSales = recentOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long totalOrders = recentOrders.size();
        long deliveredOrders = recentOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .count();
        
        // Calculate average order value
        BigDecimal averageOrderValue = deliveredOrders > 0
                ? totalSales.divide(BigDecimal.valueOf(deliveredOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Count orders by status
        Map<String, Long> ordersByStatus = recentOrders.stream()
                .collect(Collectors.groupingBy(
                    order -> order.getStatus().name(),
                    Collectors.counting()
                ));
        
        analytics.put("totalSales", totalSales);
        analytics.put("totalOrders", totalOrders);
        analytics.put("deliveredOrders", deliveredOrders);
        analytics.put("averageOrderValue", averageOrderValue);
        analytics.put("ordersByStatus", ordersByStatus);
        analytics.put("periodDays", days);
        
        return analytics;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getOutOfStockProducts() {
        return productRepository.findAll().stream()
                .filter(product -> product.getStock() <= 0)
                .map(ProductMapper::toResponse)
                .collect(Collectors.toList());
    }
}

