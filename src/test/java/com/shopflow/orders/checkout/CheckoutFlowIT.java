package com.shopflow.orders.checkout;

import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import com.shopflow.orders.AbstractIntegrationTest;
import com.shopflow.orders.ApiClient;
import com.shopflow.orders.inventory.InventoryService;
import com.shopflow.orders.payment.PaymentRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CheckoutFlowIT extends AbstractIntegrationTest {

	@Autowired
	private InventoryService inventoryService;

	@Autowired
	private PaymentRepository paymentRepository;

	@Test
	void cardCheckoutWithPromoCodeAndSeveralItems() throws Exception {
		ApiClient api = new ApiClient(mockMvc, newCustomerId());
		int keyboardsBefore = inventoryService.available(1002L);

		api.addToCart(1002L, 1).addToCart(1004L, 1).addToCart(1005L, 3).addToCart(1002L, 1);
		api.applyPromoCode("sale15").andExpect(status().isOk());

		String body = api.checkout("CARD", "tok_ok")
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("PAID"))
			.andReturn()
			.getResponse()
			.getContentAsString();
		String orderId = JsonPath.read(body, "$.orderId");

		// 2490*2 + 5490 + 390*3 = 11640; -15% = 1746
		api.order(orderId)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items.length()").value(3))
			.andExpect(jsonPath("$.subtotal").value(closeTo(11640.00, 0.001)))
			.andExpect(jsonPath("$.discount").value(closeTo(1746.00, 0.001)))
			.andExpect(jsonPath("$.total").value(closeTo(9894.00, 0.001)))
			.andExpect(jsonPath("$.items[0].quantity").value(2));

		assertThat(inventoryService.available(1002L)).isEqualTo(keyboardsBefore - 2);
		api.cart().andExpect(jsonPath("$.items.length()").value(0));
	}

	@Test
	void declinedCardReleasesStockAndKeepsCart() throws Exception {
		ApiClient api = new ApiClient(mockMvc, newCustomerId());
		int before = inventoryService.available(1005L);
		api.addToCart(1005L, 2);

		String orderId = api.checkoutAndGetOrderId("CARD", "tok_declined");

		api.order(orderId).andExpect(jsonPath("$.status").value("PAYMENT_FAILED"));
		assertThat(inventoryService.available(1005L)).isEqualTo(before);
		api.cart().andExpect(jsonPath("$.items.length()").value(1));
	}

	@Test
	void outOfStockDoesNotLeakReservationsOfOtherItems() throws Exception {
		ApiClient api = new ApiClient(mockMvc, newCustomerId());
		int miceBefore = inventoryService.available(1002L);
		int monitorsBefore = inventoryService.available(1003L);
		api.addToCart(1002L, 1).addToCart(1003L, monitorsBefore + 1);

		api.checkout("CARD", "tok_ok")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("OUT_OF_STOCK"));

		assertThat(inventoryService.available(1002L)).isEqualTo(miceBefore);
		assertThat(inventoryService.available(1003L)).isEqualTo(monitorsBefore);
	}

	@Test
	void sbpPaymentIsConfirmedByCallbackExactlyOnce() throws Exception {
		ApiClient api = new ApiClient(mockMvc, newCustomerId());
		api.addToCart(1005L, 1);

		String body = api.checkout("SBP", null)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("AWAITING_PAYMENT"))
			.andExpect(jsonPath("$.payment.qrPayload").isNotEmpty())
			.andReturn()
			.getResponse()
			.getContentAsString();
		String orderId = JsonPath.read(body, "$.orderId");
		String externalId = paymentRepository.findByOrderId(UUID.fromString(orderId)).get(0).getExternalId();

		api.paymentCallback(externalId, "SUCCEEDED").andExpect(status().isOk());
		api.order(orderId).andExpect(jsonPath("$.status").value("PAID"));

		api.changeStatus(orderId, "SHIPPED").andExpect(status().isOk());
		// провайдер повторно доставил то же уведомление
		api.paymentCallback(externalId, "SUCCEEDED").andExpect(status().isOk());

		api.order(orderId).andExpect(jsonPath("$.status").value("SHIPPED"));
	}

	@Test
	void cryptoPaymentIssuesInvoiceInBitcoin() throws Exception {
		ApiClient api = new ApiClient(mockMvc, newCustomerId());
		api.addToCart(1004L, 1);

		api.checkout("CRYPTO", null)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status").value("AWAITING_PAYMENT"))
			.andExpect(jsonPath("$.total").value(closeTo(5572.35, 0.001)))
			.andExpect(jsonPath("$.payment.cryptoCurrency").value("BTC"))
			.andExpect(jsonPath("$.payment.cryptoAmount").value(greaterThan(0.0)));
	}

}
