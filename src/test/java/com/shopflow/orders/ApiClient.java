package com.shopflow.orders;

import com.jayway.jsonpath.JsonPath;
import com.shopflow.orders.common.web.Headers;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тонкая обёртка над MockMvc, чтобы сценарные тесты читались как шаги пользователя.
 */
public record ApiClient(MockMvc mockMvc, long customerId) {

	public ApiClient addToCart(long productId, int quantity) throws Exception {
		mockMvc.perform(post("/api/v1/cart/items").header(Headers.CUSTOMER_ID, customerId)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"productId\": %d, \"quantity\": %d}".formatted(productId, quantity)))
			.andExpect(status().isOk());
		return this;
	}

	public ResultActions applyPromoCode(String code) throws Exception {
		return mockMvc.perform(put("/api/v1/cart/promo-code").header(Headers.CUSTOMER_ID, customerId)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"code\": \"%s\"}".formatted(code)));
	}

	public ResultActions cart() throws Exception {
		return mockMvc.perform(get("/api/v1/cart").header(Headers.CUSTOMER_ID, customerId));
	}

	public ResultActions checkout(String paymentMethod, String paymentToken) throws Exception {
		String token = paymentToken == null ? "null" : "\"" + paymentToken + "\"";
		return mockMvc.perform(post("/api/v1/checkout").header(Headers.CUSTOMER_ID, customerId)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"paymentMethod\": \"%s\", \"paymentToken\": %s}".formatted(paymentMethod, token)));
	}

	public String checkoutAndGetOrderId(String paymentMethod, String paymentToken) throws Exception {
		String body = checkout(paymentMethod, paymentToken).andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return JsonPath.read(body, "$.orderId");
	}

	public ResultActions order(String orderId) throws Exception {
		return mockMvc.perform(get("/api/v1/orders/{id}", orderId).header(Headers.CUSTOMER_ID, customerId));
	}

	public ResultActions cancel(String orderId) throws Exception {
		return mockMvc.perform(post("/api/v1/orders/{id}/cancel", orderId).header(Headers.CUSTOMER_ID, customerId));
	}

	public ResultActions changeStatus(String orderId, String status) throws Exception {
		return mockMvc.perform(patch("/api/v1/orders/{id}/status", orderId).contentType(MediaType.APPLICATION_JSON)
			.content("{\"status\": \"%s\"}".formatted(status)));
	}

	public ResultActions paymentCallback(String externalId, String status) throws Exception {
		return mockMvc.perform(post("/api/v1/payments/callback").contentType(MediaType.APPLICATION_JSON)
			.content("{\"externalId\": \"%s\", \"status\": \"%s\"}".formatted(externalId, status)));
	}

}
