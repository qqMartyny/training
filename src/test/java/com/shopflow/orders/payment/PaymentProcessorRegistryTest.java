package com.shopflow.orders.payment;

import java.util.List;

import com.shopflow.orders.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentProcessorRegistryTest {

	@Test
	void resolvesProcessorByMethod() {
		PaymentProcessor card = new FakeProcessor(PaymentMethod.CARD);
		PaymentProcessor sbp = new FakeProcessor(PaymentMethod.SBP);

		PaymentProcessorRegistry registry = new PaymentProcessorRegistry(List.of(card, sbp));

		assertThat(registry.get(PaymentMethod.CARD)).isSameAs(card);
		assertThat(registry.get(PaymentMethod.SBP)).isSameAs(sbp);
	}

	@Test
	void failsForUnsupportedMethod() {
		PaymentProcessorRegistry registry = new PaymentProcessorRegistry(List.of(new FakeProcessor(PaymentMethod.CARD)));

		assertThatThrownBy(() -> registry.get(PaymentMethod.CRYPTO)).isInstanceOf(BusinessException.class)
			.hasMessageContaining("CRYPTO");
	}

	@Test
	void failsFastOnDuplicateProcessors() {
		assertThatThrownBy(() -> new PaymentProcessorRegistry(
				List.of(new FakeProcessor(PaymentMethod.CARD), new FakeProcessor(PaymentMethod.CARD))))
			.isInstanceOf(IllegalStateException.class);
	}

	private record FakeProcessor(PaymentMethod method) implements PaymentProcessor {

		@Override
		public PaymentResult process(PaymentRequest request) {
			return PaymentResult.succeeded("fake");
		}

	}

}
