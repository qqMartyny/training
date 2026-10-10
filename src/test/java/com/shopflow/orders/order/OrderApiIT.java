package com.shopflow.orders.order;

import com.shopflow.orders.AbstractIntegrationTest;
import com.shopflow.orders.ApiClient;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderApiIT extends AbstractIntegrationTest {

	@Test
	void returnsOrderWithItems() throws Exception {
		ApiClient api = new ApiClient(mockMvc, newCustomerId());
		api.addToCart(1005L, 1).addToCart(1002L, 1);
		String orderId = api.checkoutAndGetOrderId("CARD", "tok_ok");

		api.order(orderId)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(orderId))
			.andExpect(jsonPath("$.items.length()").value(2));
	}

	@Test
	void customerCannotSeeSomeoneElsesOrder() throws Exception {
		ApiClient owner = new ApiClient(mockMvc, newCustomerId());
		owner.addToCart(1005L, 1);
		String orderId = owner.checkoutAndGetOrderId("CARD", "tok_ok");

		ApiClient stranger = new ApiClient(mockMvc, newCustomerId());
		stranger.order(orderId).andExpect(status().isNotFound());
		stranger.cancel(orderId).andExpect(status().isNotFound());
	}

	@Test
	void deliveredOrderCannotBeCancelled() throws Exception {
		ApiClient api = new ApiClient(mockMvc, newCustomerId());
		api.addToCart(1005L, 1);
		String orderId = api.checkoutAndGetOrderId("CARD", "tok_ok");
		api.changeStatus(orderId, "SHIPPED").andExpect(status().isOk());
		api.changeStatus(orderId, "DELIVERED").andExpect(status().isOk());

		api.cancel(orderId)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("ILLEGAL_STATUS_TRANSITION"));
	}

	@Test
	void unknownOrderReturns404() throws Exception {
		new ApiClient(mockMvc, newCustomerId()).order("00000000-0000-0000-0000-000000000000")
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}

}
