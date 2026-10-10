package com.shopflow.orders.payment;

/**
 * Проведение оплаты у платёжного провайдера.
 */
public interface PaymentProcessor {

	PaymentResult process(PaymentRequest request);

}
