package com.shopflow.orders.catalog;

import java.math.BigDecimal;

import com.shopflow.orders.AbstractIntegrationTest;
import com.shopflow.orders.ApiClient;
import com.shopflow.orders.inventory.StockItem;
import com.shopflow.orders.inventory.StockRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductCacheIT extends AbstractIntegrationTest {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private StockRepository stockRepository;

	@Test
	void priceChangeIsVisibleImmediately() throws Exception {
		Product product = productRepository
			.save(new Product("CACHE-" + System.nanoTime(), "Webcam", new BigDecimal("3000.00")));
		stockRepository.save(new StockItem(product.getId(), 10));

		mockMvc.perform(get("/api/v1/products/{id}", product.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.price").value(closeTo(3000.00, 0.001)));

		mockMvc.perform(patch("/api/v1/products/{id}/price", product.getId()).contentType(MediaType.APPLICATION_JSON)
			.content("{\"price\": 2500.00}")).andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/products/{id}", product.getId()))
			.andExpect(jsonPath("$.price").value(closeTo(2500.00, 0.001)));
		new ApiClient(mockMvc, newCustomerId()).addToCart(product.getId(), 1)
			.cart()
			.andExpect(jsonPath("$.total").value(closeTo(2500.00, 0.001)));
	}

	@Test
	void unknownProductReturns404() throws Exception {
		mockMvc.perform(get("/api/v1/products/{id}", 999_999)).andExpect(status().isNotFound());
	}

}
