package com.shopflow.orders.payment;

import java.math.BigDecimal;

/**
 * Ответ платёжного провайдера.
 *
 * @param redirectUrl страница 3-D Secure (CARD)
 * @param qrPayload содержимое QR-кода (SBP)
 * @param cryptoAddress адрес кошелька для перевода (CRYPTO)
 * @param cryptoAmount сумма к переводу в криптовалюте (CRYPTO)
 */
public record PaymentResult(PaymentStatus status, String externalId, String redirectUrl, String qrPayload,
		String cryptoAddress, BigDecimal cryptoAmount, String cryptoCurrency, String failureReason) {

	public static PaymentResult succeeded(String externalId) {
		return new PaymentResult(PaymentStatus.SUCCEEDED, externalId, null, null, null, null, null, null);
	}

	public static PaymentResult failed(String externalId, String reason) {
		return new PaymentResult(PaymentStatus.FAILED, externalId, null, null, null, null, null, reason);
	}

	public static PaymentResult pendingSbp(String externalId, String qrPayload) {
		return new PaymentResult(PaymentStatus.PENDING, externalId, null, qrPayload, null, null, null, null);
	}

	public static PaymentResult pendingCrypto(String externalId, String address, BigDecimal amount, String currency) {
		return new PaymentResult(PaymentStatus.PENDING, externalId, null, null, address, amount, currency, null);
	}

}
