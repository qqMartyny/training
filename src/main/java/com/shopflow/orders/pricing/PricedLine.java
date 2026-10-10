package com.shopflow.orders.pricing;

import java.math.BigDecimal;

/**
 * Позиция с распределённой скидкой. Нужна для фискального чека (54-ФЗ):
 * скидка должна быть указана по каждой позиции.
 */
public record PricedLine(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal,
		BigDecimal discount) {

	public BigDecimal totalAfterDiscount() {
		return lineTotal.subtract(discount);
	}

}
