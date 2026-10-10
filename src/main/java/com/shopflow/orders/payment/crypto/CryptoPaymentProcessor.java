package com.shopflow.orders.payment.crypto;

import java.math.BigDecimal;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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

	private final CryptoPaymentProperties properties;

	public CryptoPaymentProcessor(CryptoPaymentProperties properties) {
		this.properties = properties;
	}

	@Override
	public PaymentResult process(PaymentRequest request) {
		BigDecimal cryptoAmount = request.amount().divide(properties.rubRate(), Money.SCALE, Money.ROUNDING);
		String externalId = "crypto_" + UUID.randomUUID();
		return PaymentResult.pendingCrypto(externalId, newDepositAddress(), cryptoAmount, properties.currency());
	}

	private String newDepositAddress() {
		byte[] bytes = new byte[20];
		ThreadLocalRandom.current().nextBytes(bytes);
		return "bc1q" + HexFormat.of().formatHex(bytes);
	}

}
