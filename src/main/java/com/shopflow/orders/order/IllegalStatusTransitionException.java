package com.shopflow.orders.order;

import com.shopflow.orders.common.exception.ConflictException;

public class IllegalStatusTransitionException extends ConflictException {

	public IllegalStatusTransitionException(OrderStatus from, OrderStatus to) {
		super("ILLEGAL_STATUS_TRANSITION", "Cannot change order status from " + from + " to " + to);
	}

}
