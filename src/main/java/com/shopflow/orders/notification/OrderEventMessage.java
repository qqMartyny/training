package com.shopflow.orders.notification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.shopflow.orders.order.Order;
import com.shopflow.orders.order.OrderStatus;

/**
 * Публичный контракт сообщения в топике order-events (см. docs/api/asyncapi.yaml).
 */
public record OrderEventMessage(UUID eventId, UUID orderId, Long customerId, String previousStatus, String status,
		BigDecimal total, Instant occurredAt) {

	static OrderEventMessage from(Order order, OrderStatus previousStatus) {
		return new OrderEventMessage(UUID.randomUUID(), order.getId(), order.getCustomerId(),
				previousStatus == null ? null : previousStatus.name(), order.getStatus().name(), order.getTotal(),
				Instant.now());
	}

}
