package com.shopflow.orders.order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

	@EntityGraph(attributePaths = "items")
	Optional<Order> findWithItemsByIdAndCustomerId(UUID id, Long customerId);

	List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

}
