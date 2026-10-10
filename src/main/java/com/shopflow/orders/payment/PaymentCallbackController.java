package com.shopflow.orders.payment;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentCallbackController {

	private final PaymentService paymentService;

	public PaymentCallbackController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping("/callback")
	public ResponseEntity<Void> callback(@Valid @RequestBody PaymentCallbackRequest request) {
		paymentService.handleCallback(request);
		return ResponseEntity.ok().build();
	}

}
