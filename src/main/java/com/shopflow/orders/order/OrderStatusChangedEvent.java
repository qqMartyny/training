package com.shopflow.orders.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Доменное событие: статус заказа изменился. Публикуется внутри приложения,
 * во внешний мир (Kafka) уходит через {@code notification} модуль.
 */
public record OrderStatusChangedEvent(UUID eventId, UUID orderId, Long customerId, OrderStatus previousStatus,
		OrderStatus status, BigDecimal total, Instant occurredAt) {

	static OrderStatusChangedEvent of(Order order, OrderStatus previousStatus) {
		return new OrderStatusChangedEvent(UUID.randomUUID(), order.getId(), order.getCustomerId(), previousStatus,
				order.getStatus(), order.getTotal(), Instant.now());
	}

}
