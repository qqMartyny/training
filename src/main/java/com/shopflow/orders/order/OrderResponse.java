package com.shopflow.orders.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.shopflow.orders.payment.PaymentMethod;

public record OrderResponse(UUID id, OrderStatus status, PaymentMethod paymentMethod, String promoCode,
		BigDecimal subtotal, BigDecimal discount, BigDecimal paymentFee, BigDecimal total, List<Item> items,
		Instant createdAt) {

	public record Item(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal discount) {
	}

	public static OrderResponse from(Order order) {
		List<Item> items = order.getItems()
			.stream()
			.map(i -> new Item(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity(), i.getDiscount()))
			.toList();
		return new OrderResponse(order.getId(), order.getStatus(), order.getPaymentMethod(), order.getPromoCode(),
				order.getSubtotal(), order.getDiscount(), order.getPaymentFee(), order.getTotal(), items,
				order.getCreatedAt());
	}

}
