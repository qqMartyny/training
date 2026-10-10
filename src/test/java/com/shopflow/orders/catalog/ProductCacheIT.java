package com.shopflow.orders.catalog;

import com.shopflow.orders.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductCacheIT extends AbstractIntegrationTest {

	@Test
	void returnsProduct() throws Exception {
		mockMvc.perform(get("/api/v1/products/{id}", 1001))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sku").value("KB-MECH-01"));

		// второй запрос — из кэша
		mockMvc.perform(get("/api/v1/products/{id}", 1001)).andExpect(status().isOk());
	}

}
