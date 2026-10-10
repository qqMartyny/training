package com.shopflow.orders.common.web;

public final class Headers {

	/**
	 * Идентификатор покупателя. Проставляется API Gateway после проверки JWT,
	 * сервис доверяет этому заголовку (см. docs/ARCHITECTURE.md, раздел «Безопасность»).
	 */
	public static final String CUSTOMER_ID = "X-Customer-Id";

	private Headers() {
	}

}
