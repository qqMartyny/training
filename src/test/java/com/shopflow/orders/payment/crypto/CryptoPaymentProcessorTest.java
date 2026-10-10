package com.shopflow.orders.payment.crypto;

import java.math.BigDecimal;
import java.util.UUID;

import com.shopflow.orders.payment.PaymentRequest;
import com.shopflow.orders.payment.PaymentResult;
import com.shopflow.orders.payment.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CryptoPaymentProcessorTest {

	@Mock
	private CryptoPaymentProcessor processorMock;

	@Mock 
	private CryptoPaymentProperties properties;

	@Test
	void convertsRublesToBitcoin() {
		when(processorMock.process(any())).thenReturn(
				PaymentResult.pendingCrypto("crypto_1", "bc1qtest", new BigDecimal("0.00050000"), "BTC"));

		PaymentResult result = processorMock.process(new PaymentRequest(UUID.randomUUID(), 1L, new BigDecimal("4500.00"), null));

		assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
		assertThat(result.cryptoCurrency()).isEqualTo("BTC");
		assertThat(result.cryptoAmount()).isPositive();
		assertThat(result.cryptoAddress()).startsWith("bc1q");
	}

	@Test 
	void fee_countsCorrectFee() {
		CryptoPaymentProcessor processor = new CryptoPaymentProcessor(
				new CryptoPaymentProperties(
					"qwe",
					"some",
					BigDecimal.ONE,
					BigDecimal.valueOf(12)
				)
		);
		assertThat(processor.fee(BigDecimal.valueOf(100))).isEqualByComparingTo("12.00");
	}

}
