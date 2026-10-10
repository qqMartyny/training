package com.shopflow.orders.checkout;

import java.math.BigDecimal;

import com.shopflow.orders.cart.CartQuote;
import com.shopflow.orders.cart.CartService;
import com.shopflow.orders.inventory.InventoryService;
import com.shopflow.orders.order.Order;
import com.shopflow.orders.order.OrderService;
import com.shopflow.orders.order.OrderStatus;
import com.shopflow.orders.payment.PaymentFeeCalculator;
import com.shopflow.orders.payment.PaymentMethod;
import com.shopflow.orders.payment.PaymentResult;
import com.shopflow.orders.payment.PaymentService;
import com.shopflow.orders.payment.PaymentStatus;
import com.shopflow.orders.pricing.PricedLine;
import com.shopflow.orders.pricing.PricingService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Оформление заказа: корзина → цены → заказ + резерв → оплата → итоговый статус.
 */
@Service
public class CheckoutService {

	private final CartService cartService;

	private final PricingService pricingService;

	private final OrderService orderService;

	private final InventoryService inventoryService;

	private final PaymentService paymentService;

	private final PaymentFeeCalculator feeCalculator;

	public CheckoutService(CartService cartService, PricingService pricingService, OrderService orderService,
			InventoryService inventoryService, PaymentService paymentService, PaymentFeeCalculator feeCalculator) {
		this.cartService = cartService;
		this.pricingService = pricingService;
		this.orderService = orderService;
		this.inventoryService = inventoryService;
		this.paymentService = paymentService;
		this.feeCalculator = feeCalculator;
	}

	public CheckoutResponse checkout(Long customerId, CheckoutRequest request) {
		CartQuote quote = pricingService.quoteForCustomer(customerId);
		BigDecimal fee = feeCalculator.calculate(request.paymentMethod(), quote.breakdown().total());

		Order order = createOrderAndReserveStock(quote, request.paymentMethod(), fee);

		PaymentResult result = paymentService.pay(order.getId(), customerId, request.paymentMethod(),
				order.getTotal(), request.paymentToken());

		OrderStatus status = result.status() == PaymentStatus.SUCCEEDED ? OrderStatus.PAID
				: OrderStatus.PAYMENT_FAILED;
		orderService.changeStatus(order.getId(), status);

		if (status == OrderStatus.PAID) {
			cartService.clear(customerId);
		}
		return new CheckoutResponse(order.getId(), status, order.getTotal(),
				CheckoutResponse.PaymentInfo.of(request.paymentMethod(), result));
	}

	/**
	 * Заказ и резерв должны создаваться атомарно: либо всё, либо ничего.
	 */
	@Transactional
	public Order createOrderAndReserveStock(CartQuote quote, PaymentMethod paymentMethod, BigDecimal fee) {
		Order order = orderService.createOrder(quote, paymentMethod, fee);
		for (PricedLine line : quote.breakdown().lines()) {
			inventoryService.reserve(line.productId(), line.quantity());
		}
		return order;
	}

}
