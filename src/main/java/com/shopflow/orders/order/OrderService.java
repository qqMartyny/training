package com.shopflow.orders.order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.shopflow.orders.cart.CartQuote;
import com.shopflow.orders.common.exception.NotFoundException;
import com.shopflow.orders.inventory.InventoryService;
import com.shopflow.orders.inventory.StockLine;
import com.shopflow.orders.payment.PaymentMethod;
import com.shopflow.orders.pricing.PricedLine;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

	private final OrderRepository orderRepository;

	private final InventoryService inventoryService;

	private final ApplicationEventPublisher eventPublisher;

	public OrderService(OrderRepository orderRepository, InventoryService inventoryService,
			ApplicationEventPublisher eventPublisher) {
		this.orderRepository = orderRepository;
		this.inventoryService = inventoryService;
		this.eventPublisher = eventPublisher;
	}

	/**
	 * Создаёт заказ в статусе NEW и резервирует товар. Либо всё, либо ничего.
	 */
	@Transactional
	public Order placeOrder(CartQuote quote, PaymentMethod paymentMethod, BigDecimal paymentFee) {
		var breakdown = quote.breakdown();
		Order order = new Order(quote.customerId(), paymentMethod, breakdown.promoCode(), breakdown.subtotal(),
				breakdown.discount(), paymentFee);
		for (PricedLine line : breakdown.lines()) {
			order.addItem(line.productId(), line.productName(), line.unitPrice(), line.quantity(), line.discount());
		}
		inventoryService.reserve(stockLines(order));
		orderRepository.save(order);
		eventPublisher.publishEvent(OrderStatusChangedEvent.of(order, null));
		return order;
	}

	@Transactional
	public OrderResponse changeStatus(UUID orderId, OrderStatus target) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> NotFoundException.of("Order", orderId));
		applyStatus(order, target);
		return OrderResponse.from(order);
	}

	@Transactional
	public OrderResponse cancel(Long customerId, UUID orderId) {
		Order order = findOwned(customerId, orderId);
		applyStatus(order, OrderStatus.CANCELLED);
		return OrderResponse.from(order);
	}

	@Transactional(readOnly = true)
	public OrderResponse getOrder(Long customerId, UUID orderId) {
		return OrderResponse.from(findOwned(customerId, orderId));
	}

	@Transactional(readOnly = true)
	public List<OrderSummary> listOrders(Long customerId) {
		return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(OrderSummary::from).toList();
	}

	private Order findOwned(Long customerId, UUID orderId) {
		return orderRepository.findWithItemsByIdAndCustomerId(orderId, customerId)
			.orElseThrow(() -> NotFoundException.of("Order", orderId));
	}

	private void applyStatus(Order order, OrderStatus target) {
		OrderStatus previous = order.changeStatus(target);
		if (target == OrderStatus.CANCELLED || target == OrderStatus.PAYMENT_FAILED) {
			inventoryService.release(stockLines(order));
		}
		eventPublisher.publishEvent(OrderStatusChangedEvent.of(order, previous));
	}

	private static List<StockLine> stockLines(Order order) {
		return order.getItems().stream().map(i -> new StockLine(i.getProductId(), i.getQuantity())).toList();
	}

}
