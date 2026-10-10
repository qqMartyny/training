package com.shopflow.orders.payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

	Optional<Payment> findByExternalId(String externalId);

	List<Payment> findByOrderId(UUID orderId);

}
