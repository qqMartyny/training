package com.shopflow.orders.checkout;

import com.shopflow.orders.payment.PaymentMethod;
import jakarta.validation.constraints.NotNull;

/**
 * @param paymentToken токен платёжного виджета, обязателен для CARD
 */
public record CheckoutRequest(@NotNull PaymentMethod paymentMethod, String paymentToken) {
}
