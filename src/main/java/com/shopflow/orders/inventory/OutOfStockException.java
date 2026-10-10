package com.shopflow.orders.inventory;

import com.shopflow.orders.common.exception.ConflictException;

public class OutOfStockException extends ConflictException {

	public OutOfStockException(Long productId) {
		super("OUT_OF_STOCK", "Not enough stock for product " + productId);
	}

}
