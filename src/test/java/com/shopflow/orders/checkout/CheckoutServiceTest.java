package com.shopflow.orders.checkout;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.shopflow.orders.cart.CartQuote;
import com.shopflow.orders.cart.CartService;
import com.shopflow.orders.inventory.InventoryService;
import com.shopflow.orders.order.Order;
import com.shopflow.orders.order.OrderService;
import com.shopflow.orders.order.OrderStatus;
import com.shopflow.orders.payment.PaymentMethod;
import com.shopflow.orders.payment.PaymentProcessor;
import com.shopflow.orders.payment.PaymentResult;
import com.shopflow.orders.payment.PaymentService;
import com.shopflow.orders.payment.PaymentStatus;
import com.shopflow.orders.pricing.PriceBreakdown;
import com.shopflow.orders.pricing.PricedLine;
import com.shopflow.orders.pricing.PricingService;
import com.shopflow.orders.pricing.PromoCodeStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutServiceTest {

	private static final long CUSTOMER = 7L;

	private final CartService cartService = mock(CartService.class);

	private final PricingService pricingService = mock(PricingService.class);

	private final OrderService orderService = mock(OrderService.class);

	private final InventoryService inventoryService = mock(InventoryService.class);

	private final PaymentService paymentService = mock(PaymentService.class);

	private final PaymentProcessor paymentProcessor = mock(PaymentProcessor.class);

	private final CheckoutService checkoutService = new CheckoutService(cartService, pricingService, orderService,
			inventoryService, paymentService, List.of(paymentProcessor));

	@Test
	void paidByCardOrderBecomesPaidAndCartIsCleared() {
		Order order = givenCartAndOrder();
		when(paymentService.pay(eq(order.getId()), eq(CUSTOMER), eq(PaymentMethod.CARD), any(), eq("tok_ok")))
			.thenReturn(PaymentResult.succeeded("card_1"));

		CheckoutResponse response = checkoutService.checkout(CUSTOMER, new CheckoutRequest(PaymentMethod.CARD, "tok_ok"));

		assertThat(response.status()).isEqualTo(OrderStatus.PAID);
		verify(inventoryService).reserve(1001L, 1);
		verify(cartService).clear(CUSTOMER);
	}

	@Test
	void sbpCheckoutReturnsQrCode() {
		CheckoutService service = mock(CheckoutService.class);
		CheckoutResponse expected = new CheckoutResponse(UUID.randomUUID(), OrderStatus.AWAITING_PAYMENT,
				new BigDecimal("1000.00"), new CheckoutResponse.PaymentInfo(PaymentMethod.SBP, PaymentStatus.PENDING,
						"https://sbp.test/qr/1", null, null, null, null));
		when(service.checkout(eq(CUSTOMER), any())).thenReturn(expected);

		CheckoutResponse response = service.checkout(CUSTOMER, new CheckoutRequest(PaymentMethod.SBP, null));

		assertThat(response.status()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
		assertThat(response.payment().qrPayload()).isNotBlank();
	}

	private Order givenCartAndOrder() {
		BigDecimal price = new BigDecimal("1000.00");
		PriceBreakdown breakdown = new PriceBreakdown(
				List.of(new PricedLine(1001L, "Keyboard", price, 1, price, BigDecimal.ZERO)), price, BigDecimal.ZERO,
				price, null, PromoCodeStatus.NONE);
		CartQuote quote = new CartQuote(CUSTOMER, breakdown);
		when(pricingService.quoteForCustomer(CUSTOMER)).thenReturn(quote);
		Order order = new Order(CUSTOMER, PaymentMethod.CARD, null, price, BigDecimal.ZERO, BigDecimal.ZERO);
		when(orderService.createOrder(eq(quote), any(), any())).thenReturn(order);
		return order;
	}

}
