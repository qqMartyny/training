package com.shopflow.orders.payment.sbp;

import java.math.BigDecimal;
import java.util.UUID;

import com.shopflow.orders.payment.PaymentMethod;
import com.shopflow.orders.payment.PaymentProcessor;
import com.shopflow.orders.payment.PaymentRequest;
import com.shopflow.orders.payment.PaymentResult;

import org.springframework.stereotype.Component;

/**
 * Оплата через Систему быстрых платежей: формируем динамический QR-код, покупатель оплачивает его
 * в приложении банка, результат приходит асинхронно на /api/v1/payments/callback.
 */
@Component
public class SbpPaymentProcessor implements PaymentProcessor {

	private static final BigDecimal KOPECKS_IN_RUBLE = BigDecimal.valueOf(100);

	private final SbpProperties properties;

	public SbpPaymentProcessor(SbpProperties properties) {
		this.properties = properties;
	}

	@Override
	public PaymentResult process(PaymentRequest request) {
		String externalId = "sbp_" + UUID.randomUUID();
		long amountInKopecks = request.amount().multiply(KOPECKS_IN_RUBLE).longValueExact();
		String qrPayload = properties.qrBaseUrl() + "/" + externalId + "?merchant=" + properties.merchantId() + "&sum="
				+ amountInKopecks + "&cur=RUB";
		return PaymentResult.pendingSbp(externalId, qrPayload);
	}

	@Override
	public PaymentMethod method() {
		return PaymentMethod.SBP;
	}

	

}
