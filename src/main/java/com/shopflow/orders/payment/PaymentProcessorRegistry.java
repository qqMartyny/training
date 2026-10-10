package com.shopflow.orders.payment;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.shopflow.orders.common.exception.BusinessException;

import org.springframework.stereotype.Component;

/**
 * Реестр стратегий оплаты: Spring собирает все бины {@link PaymentProcessor}, реестр раскладывает их по
 * {@link PaymentMethod}.
 */
@Component
public class PaymentProcessorRegistry {

	private final Map<PaymentMethod, PaymentProcessor> processors = new EnumMap<>(PaymentMethod.class);

	public PaymentProcessorRegistry(List<PaymentProcessor> processors) {
		for (PaymentProcessor processor : processors) {
			PaymentProcessor previous = this.processors.put(processor.method(), processor);
			if (previous != null) {
				throw new IllegalStateException("Two PaymentProcessors for " + processor.method() + ": "
						+ previous.getClass().getSimpleName() + " and " + processor.getClass().getSimpleName());
			}
		}
	}

	public PaymentProcessor get(PaymentMethod method) {
		PaymentProcessor processor = processors.get(method);
		if (processor == null) {
			throw new BusinessException("PAYMENT_METHOD_NOT_SUPPORTED", "Payment method is not supported: " + method);
		}
		return processor;
	}

}
