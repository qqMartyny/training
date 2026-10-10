package com.shopflow.orders.checkout;

import java.math.BigDecimal;
import java.util.UUID;

import com.shopflow.orders.order.OrderStatus;
import com.shopflow.orders.payment.PaymentMethod;
import com.shopflow.orders.payment.PaymentResult;
import com.shopflow.orders.payment.PaymentStatus;

public record CheckoutResponse(UUID orderId, OrderStatus status, BigDecimal total, PaymentInfo payment) {

	public record PaymentInfo(PaymentMethod method, PaymentStatus status, String qrPayload, String cryptoAddress,
			BigDecimal cryptoAmount, String cryptoCurrency, String failureReason) {

		static PaymentInfo of(PaymentMethod method, PaymentResult result) {
			return new PaymentInfo(method, result.status(), result.qrPayload(), result.cryptoAddress(),
					result.cryptoAmount(), result.cryptoCurrency(), result.failureReason());
		}

	}

}
