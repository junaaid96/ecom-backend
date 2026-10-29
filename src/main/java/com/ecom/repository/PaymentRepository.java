package com.ecom.repository;

import com.ecom.entity.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @EntityGraph(attributePaths = {"order", "order.user"})
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    @EntityGraph(attributePaths = {"order", "order.user"})
    Optional<Payment> findByOrderId(Long orderId);

    @Override
    @EntityGraph(attributePaths = {"order", "order.user"})
    Optional<Payment> findById(Long id);
}
