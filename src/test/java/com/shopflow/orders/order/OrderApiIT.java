package com.shopflow.orders.order;

import java.math.BigDecimal;

import com.shopflow.orders.AbstractIntegrationTest;
import com.shopflow.orders.common.web.Headers;
import com.shopflow.orders.payment.PaymentMethod;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class OrderApiIT extends AbstractIntegrationTest {

	@Autowired
	private OrderRepository orderRepository;

	@Test
	void returnsOrderWithItems() throws Exception {
		long customerId = newCustomerId();
		Order order = new Order(customerId, PaymentMethod.CARD, null, new BigDecimal("7990.00"), BigDecimal.ZERO,
				BigDecimal.ZERO);
		order.addItem(1001L, "Механическая клавиатура Keyforge K1", new BigDecimal("7990.00"), 1, BigDecimal.ZERO);
		orderRepository.save(order);

		mockMvc.perform(get("/api/v1/orders/{id}", order.getId()).header(Headers.CUSTOMER_ID, customerId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(order.getId().toString()))
			.andExpect(jsonPath("$.items.length()").value(1));
	}

	@Test
	void listsCustomerOrders() throws Exception {
		long customerId = newCustomerId();
		orderRepository.save(new Order(customerId, PaymentMethod.SBP, null, new BigDecimal("390.00"), BigDecimal.ZERO,
				BigDecimal.ZERO));

		mockMvc.perform(get("/api/v1/orders").header(Headers.CUSTOMER_ID, customerId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1));
	}

}
