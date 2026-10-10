package com.shopflow.orders.payment.card;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Заглушка эквайринга для dev/stage. Поведение управляется токеном (как в песочнице банка):
 * <ul>
 * <li>{@code tok_declined} — отказ банка;</li>
 * <li>{@code tok_error} — банк недоступен;</li>
 * <li>любой другой — успешное списание.</li>
 * </ul>
 */
@Component
class StubCardGatewayClient implements CardGatewayClient {

	@Override
	public ChargeResponse charge(UUID orderId, String paymentToken, BigDecimal amount) {
		if ("tok_error".equals(paymentToken)) {
			throw new IllegalStateException("Acquiring gateway timeout");
		}
		String transactionId = "card_" + UUID.randomUUID();
		if ("tok_declined".equals(paymentToken)) {
			return new ChargeResponse(false, transactionId, "INSUFFICIENT_FUNDS");
		}
		return new ChargeResponse(true, transactionId, null);
	}

}
