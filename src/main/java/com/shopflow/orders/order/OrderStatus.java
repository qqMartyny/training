package com.shopflow.orders.order;

/**
 * Жизненный цикл заказа. Допустимые переходы описаны в docs/SPECIFICATION.md, раздел 5.
 */
public enum OrderStatus {

	NEW, AWAITING_PAYMENT, PAID, PAYMENT_FAILED, SHIPPED, DELIVERED, CANCELLED

}
