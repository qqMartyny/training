package com.shopflow.orders.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.shopflow.orders.cart.CartQuote;
import com.shopflow.orders.common.exception.NotFoundException;
import com.shopflow.orders.notification.OrderEventPublisher;
import com.shopflow.orders.payment.PaymentMethod;
import com.shopflow.orders.pricing.PricedLine;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

	private final OrderRepository orderRepository;

	private final OrderEventPublisher orderEventPublisher;

	public OrderService(OrderRepository orderRepository, OrderEventPublisher orderEventPublisher) {
		this.orderRepository = orderRepository;
		this.orderEventPublisher = orderEventPublisher;
	}

	@Transactional
	public Order createOrder(CartQuote quote, PaymentMethod paymentMethod, BigDecimal paymentFee) {
		var breakdown = quote.breakdown();
		Order order = new Order(quote.customerId(), paymentMethod, breakdown.promoCode(), breakdown.subtotal(),
				breakdown.discount(), paymentFee);
		for (PricedLine line : breakdown.lines()) {
			order.addItem(line.productId(), line.productName(), line.unitPrice(), line.quantity(), line.discount());
		}
		orderRepository.save(order);
		orderEventPublisher.publish(order, null);
		return order;
	}

	@Transactional
	public Order changeStatus(UUID orderId, OrderStatus status) {
		Order order = getOrder(orderId);
		OrderStatus previous = order.getStatus();
		order.setStatus(status);
		orderEventPublisher.publish(order, previous);
		return order;
	}

	@Transactional
	public Order cancel(UUID orderId) {
		return changeStatus(orderId, OrderStatus.CANCELLED);
	}

	public Order getOrder(UUID orderId) {
		return orderRepository.findById(orderId).orElseThrow(() -> NotFoundException.of("Order", orderId));
	}

	@Transactional(readOnly = true)
	public List<OrderSummary> listOrders(Long customerId) {
		return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(OrderSummary::from).toList();
	}

}
