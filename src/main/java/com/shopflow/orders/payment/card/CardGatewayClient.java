package com.shopflow.orders.payment.card;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Клиент эквайринга банка-партнёра.
 */
public interface CardGatewayClient {

	ChargeResponse charge(UUID orderId, String paymentToken, BigDecimal amount);

	record ChargeResponse(boolean approved, String transactionId, String declineReason) {
	}

}
