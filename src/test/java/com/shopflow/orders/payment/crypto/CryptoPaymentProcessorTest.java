package com.shopflow.orders.payment.crypto;

import java.math.BigDecimal;
import java.util.UUID;

import com.shopflow.orders.payment.PaymentRequest;
import com.shopflow.orders.payment.PaymentResult;
import com.shopflow.orders.payment.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoPaymentProcessorTest {

	private final CryptoPaymentProcessor processor = new CryptoPaymentProcessor(
			new CryptoPaymentProperties("test-key", "BTC", new BigDecimal("9000000"), new BigDecimal("1.5")));

	@Test
	void convertsRublesToBitcoinWithSatoshiPrecision() {
		PaymentResult result = processor.process(new PaymentRequest(UUID.randomUUID(), 1L, new BigDecimal("4500.00"), null));

		assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
		assertThat(result.cryptoCurrency()).isEqualTo("BTC");
		assertThat(result.cryptoAmount()).isEqualByComparingTo("0.00050000");
		assertThat(result.cryptoAmount().scale()).isEqualTo(8);
		assertThat(result.cryptoAddress()).startsWith("bc1q");
	}

	@Test
	void roundsUpSoThatMerchantIsNotUnderpaid() {
		PaymentResult result = processor.process(new PaymentRequest(UUID.randomUUID(), 1L, new BigDecimal("1000.00"), null));

		// 1000 / 9 000 000 = 0.000111111(1)...
		assertThat(result.cryptoAmount()).isEqualByComparingTo("0.00011112");
	}

	@Test
	void chargesServiceFee() {
		assertThat(processor.fee(new BigDecimal("1000.00"))).isEqualByComparingTo("15.00");
	}

}
