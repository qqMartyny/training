package com.shopflow.orders.pricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Распределяет скидку по позициям заказа (нужно для фискального чека, 54-ФЗ).
 */
@Component
public class DiscountAllocator {

	public List<PricedLine> allocate(List<PriceLine> lines, BigDecimal discount, BigDecimal subtotal) {
		if (lines.isEmpty()) {
			return List.of();
		}
		BigDecimal perLine = discount.divide(BigDecimal.valueOf(lines.size()));
		List<PricedLine> result = new ArrayList<>(lines.size());
		for (PriceLine line : lines) {
			result.add(new PricedLine(line.productId(), line.productName(), line.unitPrice(), line.quantity(),
					line.lineTotal(), perLine));
		}
		return result;
	}

}
