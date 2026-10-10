package com.shopflow.orders.notification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.shopflow.orders.order.OrderStatusChangedEvent;

/**
 * Публичный контракт сообщения в топике order-events (см. docs/api/asyncapi.yaml).
 * Отделён от доменного события, чтобы рефакторинг домена не ломал потребителей.
 */
public record OrderEventMessage(UUID eventId, UUID orderId, Long customerId, String previousStatus, String status,
		BigDecimal total, Instant occurredAt) {

	static OrderEventMessage from(OrderStatusChangedEvent event) {
		return new OrderEventMessage(event.eventId(), event.orderId(), event.customerId(),
				event.previousStatus() == null ? null : event.previousStatus().name(), event.status().name(),
				event.total(), event.occurredAt());
	}

}
