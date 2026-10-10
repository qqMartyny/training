package com.shopflow.orders.payment;

public enum PaymentStatus {

	PENDING, SUCCEEDED, FAILED;

	public boolean isFinal() {
		return this != PENDING;
	}

}
