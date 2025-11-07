package com.ecom.repository;

import com.ecom.entity.Order;
import com.ecom.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    List<Order> findAllByUser(User user);

    @Override
    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    List<Order> findAll();

    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    java.util.Optional<Order> findById(Long id);
}

