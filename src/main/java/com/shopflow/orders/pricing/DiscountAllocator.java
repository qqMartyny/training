package com.shopflow.orders.pricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Распределяет скидку по позициям пропорционально их стоимости.
 * Остаток от округления уходит в последнюю позицию, чтобы сумма скидок по позициям
 * совпадала с общей скидкой до копейки.
 */
@Component
public class DiscountAllocator {

	public List<PricedLine> allocate(List<PriceLine> lines, BigDecimal discount, BigDecimal subtotal) {
		List<PricedLine> result = new ArrayList<>(lines.size());
		BigDecimal allocated = BigDecimal.ZERO;
		for (int i = 0; i < lines.size(); i++) {
			PriceLine line = lines.get(i);
			BigDecimal lineTotal = line.lineTotal();
			BigDecimal lineDiscount;
			if (i == lines.size() - 1) {
				lineDiscount = discount.subtract(allocated);
			}
			else if (subtotal.signum() == 0) {
				lineDiscount = BigDecimal.ZERO.setScale(Money.SCALE);
			}
			else {
				lineDiscount = discount.multiply(lineTotal).divide(subtotal, Money.SCALE, Money.ROUNDING);
			}
			allocated = allocated.add(lineDiscount);
			result.add(new PricedLine(line.productId(), line.productName(), line.unitPrice(), line.quantity(), lineTotal,
					lineDiscount));
		}
		return result;
	}

}
