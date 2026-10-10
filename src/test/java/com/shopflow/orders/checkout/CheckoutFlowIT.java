package com.shopflow.orders.checkout;

import com.shopflow.orders.AbstractIntegrationTest;
import com.shopflow.orders.ApiClient;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CheckoutFlowIT extends AbstractIntegrationTest {

	@Test
	void cardCheckout() throws Exception {
		ApiClient api = new ApiClient(mockMvc, newCustomerId());
		api.addToCart(1002L, 1).addToCart(1005L, 2);

		api.checkout("CARD", "tok_ok")
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.orderId").isNotEmpty())
			.andExpect(jsonPath("$.status").value("PAID"));
	}

	@Test
	void sbpCheckout() throws Exception {
		ApiClient api = new ApiClient(mockMvc, newCustomerId());
		api.addToCart(1005L, 1);

		api.checkout("SBP", null).andExpect(status().isCreated());
	}

}
