package com.shopflow.orders.payment.crypto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import com.shopflow.orders.payment.PaymentMethod;
import com.shopflow.orders.payment.PaymentProcessor;
import com.shopflow.orders.payment.PaymentRequest;
import com.shopflow.orders.payment.PaymentResult;
import com.shopflow.orders.pricing.Money;

import org.springframework.stereotype.Component;

/**
 * Оплата криптовалютой: выставляем счёт на уникальный адрес, подтверждение сети приходит асинхронно на
 * /api/v1/payments/callback.
 */
@Component
public class CryptoPaymentProcessor implements PaymentProcessor {

	/** Минимальная единица BTC — сатоши, 10^-8. */
	static final int CRYPTO_SCALE = 8;

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	private final CryptoPaymentProperties properties;

	public CryptoPaymentProcessor(CryptoPaymentProperties properties) {
		this.properties = properties;
	}

	@Override
	public PaymentMethod method() {
		return PaymentMethod.CRYPTO;
	}

	@Override
	public BigDecimal fee(BigDecimal amount) {
		return amount.multiply(properties.feePercent()).divide(HUNDRED, Money.SCALE, Money.ROUNDING);
	}

	@Override
	public PaymentResult process(PaymentRequest request) {
		BigDecimal cryptoAmount = request.amount().divide(properties.rubRate(), CRYPTO_SCALE, RoundingMode.UP);
		String externalId = "crypto_" + UUID.randomUUID();
		return PaymentResult.pendingCrypto(externalId, newDepositAddress(), cryptoAmount, properties.currency());
	}

	private String newDepositAddress() {
		byte[] bytes = new byte[20];
		ThreadLocalRandom.current().nextBytes(bytes);
		return "bc1q" + HexFormat.of().formatHex(bytes);
	}

}
