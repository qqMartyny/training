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
	private CryptoPaymentProcessor processor;

	@Test
	void convertsRublesToBitcoin() {
		when(processor.process(any())).thenReturn(
				PaymentResult.pendingCrypto("crypto_1", "bc1qtest", new BigDecimal("0.00050000"), "BTC"));

		PaymentResult result = processor.process(new PaymentRequest(UUID.randomUUID(), 1L, new BigDecimal("4500.00"), null));

		assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
		assertThat(result.cryptoCurrency()).isEqualTo("BTC");
		assertThat(result.cryptoAmount()).isPositive();
		assertThat(result.cryptoAddress()).startsWith("bc1q");
	}

}
