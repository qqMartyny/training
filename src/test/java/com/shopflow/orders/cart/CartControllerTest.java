package com.shopflow.orders.cart;

import java.math.BigDecimal;
import java.util.List;

import com.shopflow.orders.common.web.Headers;
import com.shopflow.orders.pricing.PromoCodeStatus;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private CartService cartService;

	@Test
	void returnsCartView() throws Exception {
		when(cartService.getCart(7L)).thenReturn(new CartView(7L,
				List.of(new CartView.Line(1001L, "Keyboard", new BigDecimal("7990.00"), 1, new BigDecimal("7990.00"))),
				new BigDecimal("7990.00"), BigDecimal.ZERO, new BigDecimal("7990.00"), null, PromoCodeStatus.NONE));

		mockMvc.perform(get("/api/v1/cart").header(Headers.CUSTOMER_ID, 7))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].productId").value(1001))
			.andExpect(jsonPath("$.total").value(7990.00));
	}

	@Test
	void addsItem() throws Exception {
		when(cartService.addItem(eq(7L), any())).thenReturn(new CartView(7L, List.of(), BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO, null, PromoCodeStatus.NONE));

		mockMvc.perform(post("/api/v1/cart/items").header(Headers.CUSTOMER_ID, 7)
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"productId": 1001, "quantity": 2}
					"""))
			.andExpect(status().isOk());
	}

	@Test
	void rejectsNonPositiveQuantity() throws Exception {
		mockMvc.perform(post("/api/v1/cart/items").header(Headers.CUSTOMER_ID, 7)
			.contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"productId": 1001, "quantity": -5}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

		verifyNoInteractions(cartService);
	}

	@Test
	void requiresCustomerHeader() throws Exception {
		mockMvc.perform(get("/api/v1/cart")).andExpect(status().isBadRequest());
	}

}
