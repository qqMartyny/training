package com.shopflow.orders.payment.card;

import com.shopflow.orders.payment.PaymentMethod;
import com.shopflow.orders.payment.PaymentProcessor;
import com.shopflow.orders.payment.PaymentRequest;
import com.shopflow.orders.payment.PaymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Component
public class CardPaymentProcessor implements PaymentProcessor {

	private static final Logger log = LoggerFactory.getLogger(CardPaymentProcessor.class);

	private final CardGatewayClient gatewayClient;

	public CardPaymentProcessor(CardGatewayClient gatewayClient) {
		this.gatewayClient = gatewayClient;
	}

	@Override
	public PaymentMethod method() {
		return PaymentMethod.CARD;
	}

	@Override
	public boolean requiresPaymentToken() {
		return true;
	}

	@Override
	public PaymentResult process(PaymentRequest request) {
		log.info("Charging card: {}", request);
		CardGatewayClient.ChargeResponse response = gatewayClient.charge(request.orderId(), request.paymentToken(),
				request.amount());
		if (!response.approved()) {
			return PaymentResult.failed(response.transactionId(), response.declineReason());
		}
		return PaymentResult.succeeded(response.transactionId());
	}

}
