package com.shopflow.orders.checkout;

import java.math.BigDecimal;

import com.shopflow.orders.cart.CartQuote;
import com.shopflow.orders.cart.CartService;
import com.shopflow.orders.common.exception.BusinessException;
import com.shopflow.orders.order.Order;
import com.shopflow.orders.order.OrderService;
import com.shopflow.orders.order.OrderStatus;
import com.shopflow.orders.payment.PaymentProcessor;
import com.shopflow.orders.payment.PaymentProcessorRegistry;
import com.shopflow.orders.payment.PaymentResult;
import com.shopflow.orders.payment.PaymentService;
import com.shopflow.orders.payment.PaymentStatus;
import com.shopflow.orders.pricing.Money;

import org.springframework.stereotype.Service;

/**
 * Фасад оформления заказа: корзина → цены → заказ + резерв → оплата → итоговый статус.
 * <p>
 * Сознательно НЕ транзакционный: оплата — внешний вызов, держать на время него транзакцию БД нельзя.
 * Каждый шаг атомарен сам по себе, при неуспешной оплате выполняется компенсация (снятие резерва).
 */
@Service
public class CheckoutService {

	private final CartService cartService;

	private final OrderService orderService;

	private final PaymentService paymentService;

	private final PaymentProcessorRegistry paymentProcessors;

	public CheckoutService(CartService cartService, OrderService orderService, PaymentService paymentService,
			PaymentProcessorRegistry paymentProcessors) {
		this.cartService = cartService;
		this.orderService = orderService;
		this.paymentService = paymentService;
		this.paymentProcessors = paymentProcessors;
	}

	public CheckoutResponse checkout(Long customerId, CheckoutRequest request) {
		CartQuote quote = cartService.quote(customerId);
		if (quote.breakdown().promoCodeRejected()) {
			throw new BusinessException("PROMO_CODE_INVALID",
					"Promo code " + quote.breakdown().promoCode() + " is " + quote.breakdown().promoCodeStatus());
		}

		PaymentProcessor processor = paymentProcessors.get(request.paymentMethod());
		if (processor.requiresPaymentToken() && (request.paymentToken() == null || request.paymentToken().isBlank())) {
			throw new BusinessException("PAYMENT_TOKEN_REQUIRED", "paymentToken is required for " + processor.method());
		}
		BigDecimal fee = Money.of(processor.fee(quote.breakdown().total()));

		Order order = orderService.placeOrder(quote, processor.method(), fee);

		PaymentResult result = paymentService.pay(order.getId(), customerId, processor.method(), order.getTotal(),
				request.paymentToken());

		OrderStatus status = switch (result.status()) {
			case SUCCEEDED -> OrderStatus.PAID;
			case PENDING -> OrderStatus.AWAITING_PAYMENT;
			case FAILED -> OrderStatus.PAYMENT_FAILED;
		};
		orderService.changeStatus(order.getId(), status);

		if (result.status() != PaymentStatus.FAILED) {
			cartService.clear(customerId);
		}
		return new CheckoutResponse(order.getId(), status, order.getTotal(),
				CheckoutResponse.PaymentInfo.of(processor.method(), result));
	}

}
