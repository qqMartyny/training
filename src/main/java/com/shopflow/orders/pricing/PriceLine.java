package com.shopflow.orders.pricing;

import java.math.BigDecimal;

/**
 * Позиция для расчёта цены: что покупаем, по какой цене и сколько.
 */
public record PriceLine(Long productId, String productName, BigDecimal unitPrice, int quantity) {

	public BigDecimal lineTotal() {
		return Money.of(unitPrice.multiply(BigDecimal.valueOf(quantity)));
	}

}
