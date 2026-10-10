package com.shopflow.orders.payment;

import java.math.BigDecimal;
import java.util.UUID;

import com.shopflow.orders.common.exception.NotFoundException;
import com.shopflow.orders.order.OrderService;
import com.shopflow.orders.order.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

	private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

	private final PaymentProcessorRegistry registry;

	private final PaymentRepository paymentRepository;

	private final OrderService orderService;

	public PaymentService(PaymentProcessorRegistry registry, PaymentRepository paymentRepository,
			OrderService orderService) {
		this.registry = registry;
		this.paymentRepository = paymentRepository;
		this.orderService = orderService;
	}

	/**
	 * Проводит оплату у провайдера и сохраняет платёж. Вызывается вне транзакции БД:
	 * внешний HTTP-вызов не должен держать соединение с базой.
	 */
	public PaymentResult pay(UUID orderId, Long customerId, PaymentMethod method, BigDecimal amount,
			String paymentToken) {
		PaymentRequest request = new PaymentRequest(orderId, customerId, amount, paymentToken);
		PaymentResult result;
		try {
			result = registry.get(method).process(request);
		}
		catch (RuntimeException ex) {
			log.warn("Payment provider error for order {}: {}", orderId, ex.getMessage());
			result = PaymentResult.failed(null, "PROVIDER_ERROR");
		}
		paymentRepository.save(Payment.from(orderId, method, amount, result));
		log.info("Payment for order {} via {}: {}", orderId, method, result.status());
		return result;
	}

	@Transactional
	public void handleCallback(PaymentCallbackRequest callback) {
		Payment payment = paymentRepository.findByExternalId(callback.externalId())
			.orElseThrow(() -> NotFoundException.of("Payment", callback.externalId()));
		if (payment.getStatus().isFinal()) {
			log.info("Duplicate callback for payment {} ignored (status {})", payment.getId(), payment.getStatus());
			return;
		}
		if (callback.status() == PaymentStatus.PENDING) {
			return;
		}
		payment.complete(callback.status(), callback.failureReason());
		OrderStatus target = callback.status() == PaymentStatus.SUCCEEDED ? OrderStatus.PAID
				: OrderStatus.PAYMENT_FAILED;
		orderService.changeStatus(payment.getOrderId(), target);
	}

}
