package com.shopflow.orders.pricing;

import java.math.BigDecimal;
import java.util.List;

public record PriceBreakdown(List<PricedLine> lines, BigDecimal subtotal, BigDecimal discount, BigDecimal total,
		String promoCode, PromoCodeStatus promoCodeStatus) {

	public boolean promoCodeRejected() {
		return promoCodeStatus != PromoCodeStatus.NONE && promoCodeStatus != PromoCodeStatus.APPLIED;
	}

}
