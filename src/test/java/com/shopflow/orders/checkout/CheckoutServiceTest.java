package com.shopflow.orders.checkout;

import java.math.BigDecimal;
import java.util.List;

import com.shopflow.orders.cart.CartQuote;
import com.shopflow.orders.cart.CartService;
import com.shopflow.orders.common.exception.BusinessException;
import com.shopflow.orders.order.Order;
import com.shopflow.orders.order.OrderService;
import com.shopflow.orders.order.OrderStatus;
import com.shopflow.orders.payment.PaymentMethod;
import com.shopflow.orders.payment.PaymentProcessor;
import com.shopflow.orders.payment.PaymentProcessorRegistry;
import com.shopflow.orders.payment.PaymentResult;
import com.shopflow.orders.payment.PaymentService;
import com.shopflow.orders.pricing.PriceBreakdown;
import com.shopflow.orders.pricing.PricedLine;
import com.shopflow.orders.pricing.PromoCodeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CheckoutServiceTest {

	private static final long CUSTOMER = 7L;

	private final CartService cartService = mock(CartService.class);

	private final OrderService orderService = mock(OrderService.class);

	private final PaymentService paymentService = mock(PaymentService.class);

	private final PaymentProcessor card = mock(PaymentProcessor.class);

	private final PaymentProcessor sbp = mock(PaymentProcessor.class);

	private CheckoutService checkoutService;

	@BeforeEach
	void setUp() {
		when(card.method()).thenReturn(PaymentMethod.CARD);
		when(card.requiresPaymentToken()).thenReturn(true);
		when(card.fee(any())).thenReturn(BigDecimal.ZERO);
		when(sbp.method()).thenReturn(PaymentMethod.SBP);
		when(sbp.fee(any())).thenReturn(BigDecimal.ZERO);
		checkoutService = new CheckoutService(cartService, orderService, paymentService,
				new PaymentProcessorRegistry(List.of(card, sbp)));
	}

	@Test
	void paidByCardOrderBecomesPaidAndCartIsCleared() {
		Order order = givenCartAndOrder(PromoCodeStatus.NONE);
		when(paymentService.pay(eq(order.getId()), eq(CUSTOMER), eq(PaymentMethod.CARD), any(), eq("tok_ok")))
			.thenReturn(PaymentResult.succeeded("card_1"));

		CheckoutResponse response = checkoutService.checkout(CUSTOMER, new CheckoutRequest(PaymentMethod.CARD, "tok_ok"));

		assertThat(response.status()).isEqualTo(OrderStatus.PAID);
		verify(orderService).changeStatus(order.getId(), OrderStatus.PAID);
		verify(cartService).clear(CUSTOMER);
	}

	@Test
	void sbpOrderAwaitsPayment() {
		Order order = givenCartAndOrder(PromoCodeStatus.NONE);
		when(paymentService.pay(any(), any(), eq(PaymentMethod.SBP), any(), any()))
			.thenReturn(PaymentResult.pendingSbp("sbp_1", "qr"));

		CheckoutResponse response = checkoutService.checkout(CUSTOMER, new CheckoutRequest(PaymentMethod.SBP, null));

		assertThat(response.status()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
		assertThat(response.payment().qrPayload()).isEqualTo("qr");
		verify(orderService).changeStatus(order.getId(), OrderStatus.AWAITING_PAYMENT);
	}

	@Test
	void declinedPaymentFailsOrderAndKeepsCart() {
		Order order = givenCartAndOrder(PromoCodeStatus.NONE);
		when(paymentService.pay(any(), any(), any(), any(), any()))
			.thenReturn(PaymentResult.failed("card_1", "INSUFFICIENT_FUNDS"));

		CheckoutResponse response = checkoutService.checkout(CUSTOMER,
				new CheckoutRequest(PaymentMethod.CARD, "tok_declined"));

		assertThat(response.status()).isEqualTo(OrderStatus.PAYMENT_FAILED);
		verify(orderService).changeStatus(order.getId(), OrderStatus.PAYMENT_FAILED);
		verify(cartService, never()).clear(any());
	}

	@Test
	void rejectsCardPaymentWithoutToken() {
		givenCartAndOrder(PromoCodeStatus.NONE);

		assertThatThrownBy(() -> checkoutService.checkout(CUSTOMER, new CheckoutRequest(PaymentMethod.CARD, " ")))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining("paymentToken");
		verifyNoInteractions(paymentService);
	}

	@Test
	void rejectsCheckoutWithInvalidPromoCode() {
		givenCartAndOrder(PromoCodeStatus.EXPIRED);

		assertThatThrownBy(() -> checkoutService.checkout(CUSTOMER, new CheckoutRequest(PaymentMethod.SBP, null)))
			.isInstanceOf(BusinessException.class);
		verifyNoInteractions(paymentService);
	}

	private Order givenCartAndOrder(PromoCodeStatus promoStatus) {
		BigDecimal price = new BigDecimal("1000.00");
		PriceBreakdown breakdown = new PriceBreakdown(
				List.of(new PricedLine(1001L, "Keyboard", price, 1, price, BigDecimal.ZERO)), price, BigDecimal.ZERO,
				price, promoStatus == PromoCodeStatus.NONE ? null : "SALE15", promoStatus);
		CartQuote quote = new CartQuote(CUSTOMER, breakdown);
		when(cartService.quote(CUSTOMER)).thenReturn(quote);
		Order order = new Order(CUSTOMER, PaymentMethod.CARD, null, price, BigDecimal.ZERO, BigDecimal.ZERO);
		when(orderService.placeOrder(eq(quote), any(), any())).thenReturn(order);
		return order;
	}

}
