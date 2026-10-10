package com.shopflow.orders.cart;

import java.math.BigDecimal;
import java.util.List;

import com.shopflow.orders.pricing.PriceBreakdown;
import com.shopflow.orders.pricing.PromoCodeStatus;

public record CartView(Long customerId, List<Line> items, BigDecimal subtotal, BigDecimal discount, BigDecimal total,
		String promoCode, PromoCodeStatus promoCodeStatus) {

	public record Line(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
	}

	static CartView of(Long customerId, PriceBreakdown breakdown) {
		List<Line> lines = breakdown.lines()
			.stream()
			.map(l -> new Line(l.productId(), l.productName(), l.unitPrice(), l.quantity(), l.lineTotal()))
			.toList();
		return new CartView(customerId, lines, breakdown.subtotal(), breakdown.discount(), breakdown.total(),
				breakdown.promoCode(), breakdown.promoCodeStatus());
	}

}
