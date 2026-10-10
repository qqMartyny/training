package com.shopflow.orders.payment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @param paymentToken одноразовый токен платёжного виджета (только для CARD)
 */
public record PaymentRequest(UUID orderId, Long customerId, BigDecimal amount, String paymentToken) {
}
