package com.shopflow.orders.cart;

import com.shopflow.orders.pricing.PriceBreakdown;

/**
 * Снимок корзины с рассчитанными ценами — входные данные для оформления заказа.
 */
public record CartQuote(Long customerId, PriceBreakdown breakdown) {
}
