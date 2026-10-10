package com.shopflow.orders.pricing;

import java.math.BigDecimal;

/**
 * Стратегия расчёта скидки по промокоду.
 */
public interface DiscountPolicy {

	DiscountType type();

	/**
	 * @param subtotal сумма корзины без скидки
	 * @param value значение промокода (процент или фиксированная сумма)
	 * @return размер скидки, не округлённый и не ограниченный суммой корзины
	 */
	BigDecimal discount(BigDecimal subtotal, BigDecimal value);

}
