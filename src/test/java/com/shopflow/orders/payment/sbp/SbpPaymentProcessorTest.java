package com.shopflow.orders.payment.sbp;

import java.math.BigDecimal;
import java.util.UUID;

import com.shopflow.orders.payment.PaymentRequest;
import com.shopflow.orders.payment.PaymentResult;
import com.shopflow.orders.payment.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SbpPaymentProcessorTest {

	private final SbpPaymentProcessor processor = new SbpPaymentProcessor(
			new SbpProperties("MA0000012345", "https://sbp.test/qr"));

	@Test
	void createsPendingPaymentWithQrInKopecks() {
		PaymentResult result = processor.process(new PaymentRequest(UUID.randomUUID(), 1L, new BigDecimal("1234.50"), null));

		assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
		assertThat(result.externalId()).startsWith("sbp_");
		assertThat(result.qrPayload()).startsWith("https://sbp.test/qr/" + result.externalId())
			.contains("sum=123450")
			.contains("merchant=MA0000012345");
	}

}
