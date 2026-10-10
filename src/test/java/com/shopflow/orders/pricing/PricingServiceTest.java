package com.shopflow.orders.pricing;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import com.shopflow.orders.cart.CartService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PricingServiceTest {

	private static final Instant NOW = Instant.parse("2026-06-01T12:00:00Z");

	private final PromoCodeRepository promoCodeRepository = mock(PromoCodeRepository.class);

	private PricingService pricingService;

	@BeforeEach
	void setUp() {
		when(promoCodeRepository.findByCodeIgnoreCase(anyString())).thenReturn(Optional.empty());
		pricingService = new PricingService(promoCodeRepository, new DiscountCalculator(), new DiscountAllocator(),
				mock(CartService.class), Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	void calculatesSubtotalWithoutPromoCode() {
		PriceBreakdown result = pricingService.quote(
				List.of(line(1001L, "1990.00", 2), line(1002L, "390.00", 1)), null);

		assertThat(result.subtotal()).isEqualByComparingTo("4370.00");
		assertThat(result.discount()).isEqualByComparingTo("0");
		assertThat(result.total()).isEqualByComparingTo("4370.00");
		assertThat(result.promoCodeStatus()).isEqualTo(PromoCodeStatus.NONE);
	}

	@Test
	void appliesPercentagePromoCode() {
		givenPromo(new PromoCode("SALE15", "PERCENT", new BigDecimal("15"), BigDecimal.ZERO,
				NOW.minusSeconds(60), NOW.plusSeconds(60)));

		PriceBreakdown result = pricingService.quote(List.of(line(1001L, "1000.00", 1)), "SALE15");

		assertThat(result.discount()).isEqualByComparingTo("150.00");
		assertThat(result.total()).isEqualByComparingTo("850.00");
		assertThat(result.promoCodeStatus()).isEqualTo(PromoCodeStatus.APPLIED);
	}

	@Test
	void appliesFixedAmountPromoCode() {
		givenPromo(new PromoCode("MINUS500", "FIXED", new BigDecimal("500"), new BigDecimal("3000"),
				NOW.minusSeconds(60), NOW.plusSeconds(60)));

		PriceBreakdown result = pricingService.quote(List.of(line(1001L, "3500.00", 1)), "MINUS500");

		assertThat(result.discount());
		assertThat(result.total());
	}

	@Test
	void rejectsPromoCodeBelowMinimumAmount() {
		givenPromo(new PromoCode("MINUS500", "FIXED", new BigDecimal("500"), new BigDecimal("3000"),
				NOW.minusSeconds(60), NOW.plusSeconds(60)));

		PriceBreakdown result = pricingService.quote(List.of(line(1001L, "2999.99", 1)), "MINUS500");

		assertThat(result.promoCodeStatus()).isEqualTo(PromoCodeStatus.MIN_AMOUNT_NOT_REACHED);
		assertThat(result.total()).isEqualByComparingTo("2999.99");
	}

	@Test
	void rejectsExpiredPromoCode() {
		givenPromo(new PromoCode("SALE15", "PERCENT", new BigDecimal("15"), BigDecimal.ZERO,
				NOW.minusSeconds(120), NOW.minusSeconds(60)));

		PriceBreakdown result = pricingService.quote(List.of(line(1001L, "1000.00", 1)), "SALE15");

		assertThat(result.promoCodeStatus()).isEqualTo(PromoCodeStatus.EXPIRED);
		assertThat(result.discount()).isEqualByComparingTo("0");
	}

	@Test
	void reportsUnknownPromoCode() {
		PriceBreakdown result = pricingService.quote(List.of(line(1001L, "1000.00", 1)), "NOPE");

		assertThat(result.promoCodeStatus()).isEqualTo(PromoCodeStatus.NOT_FOUND);
	}

	private void givenPromo(PromoCode promo) {
		when(promoCodeRepository.findByCodeIgnoreCase(promo.getCode())).thenReturn(Optional.of(promo));
	}

	private static PriceLine line(Long productId, String price, int quantity) {
		return new PriceLine(productId, "product-" + productId, new BigDecimal(price), quantity);
	}

}
