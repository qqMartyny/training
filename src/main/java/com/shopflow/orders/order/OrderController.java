package com.shopflow.orders.order;

import java.util.List;
import java.util.UUID;

import com.shopflow.orders.common.web.Headers;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping
	public List<OrderSummary> list(@RequestHeader(Headers.CUSTOMER_ID) Long customerId) {
		return orderService.listOrders(customerId);
	}

	@GetMapping("/{orderId}")
	public OrderResponse get(@RequestHeader(Headers.CUSTOMER_ID) Long customerId, @PathVariable UUID orderId) {
		return OrderResponse.from(orderService.getOrder(orderId));
	}

	@PostMapping("/{orderId}/cancel")
	public OrderResponse cancel(@RequestHeader(Headers.CUSTOMER_ID) Long customerId, @PathVariable UUID orderId) {
		return OrderResponse.from(orderService.cancel(orderId));
	}

	/**
	 * Внутренний эндпоинт для сервиса доставки (доступен только из внутренней сети).
	 */
	@PatchMapping("/{orderId}/status")
	public OrderResponse changeStatus(@PathVariable UUID orderId, @Valid @RequestBody ChangeStatusRequest request) {
		return OrderResponse.from(orderService.changeStatus(orderId, request.status()));
	}

}
