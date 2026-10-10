package com.shopflow.orders.pricing;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import com.shopflow.orders.cart.Cart;
import com.shopflow.orders.cart.CartQuote;
import com.shopflow.orders.cart.CartService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricingService {

	private final PromoCodeRepository promoCodeRepository;

	private final DiscountCalculator discountCalculator;

	private final DiscountAllocator discountAllocator;

	private final CartService cartService;

	private final Clock clock;

	public PricingService(PromoCodeRepository promoCodeRepository, DiscountCalculator discountCalculator,
			DiscountAllocator discountAllocator, CartService cartService, Clock clock) {
		this.promoCodeRepository = promoCodeRepository;
		this.discountCalculator = discountCalculator;
		this.discountAllocator = discountAllocator;
		this.cartService = cartService;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public PriceBreakdown quote(List<PriceLine> lines, String promoCode) {
		BigDecimal subtotal = Money.of(lines.stream().map(PriceLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add));

		if (promoCode == null || promoCode.isBlank()) {
			return withoutDiscount(lines, subtotal, null, PromoCodeStatus.NONE);
		}

		Optional<PromoCode> found = promoCodeRepository.findByCodeIgnoreCase(promoCode.trim());
		if (found.isEmpty()) {
			return withoutDiscount(lines, subtotal, promoCode, PromoCodeStatus.NOT_FOUND);
		}
		PromoCode promo = found.get();
		if (!promo.isValidAt(clock.instant())) {
			return withoutDiscount(lines, subtotal, promo.getCode(), PromoCodeStatus.EXPIRED);
		}
		if (subtotal.compareTo(promo.getMinOrderAmount()) < 0) {
			return withoutDiscount(lines, subtotal, promo.getCode(), PromoCodeStatus.MIN_AMOUNT_NOT_REACHED);
		}

		BigDecimal discount = discountCalculator.calculate(promo, subtotal);
		BigDecimal total = subtotal.subtract(discount);
		return new PriceBreakdown(discountAllocator.allocate(lines, discount, subtotal), subtotal, discount, total,
				promo.getCode(), PromoCodeStatus.APPLIED);
	}

	/**
	 * SHOP-205: финальный расчёт корзины покупателя для оформления заказа.
	 */
	@Transactional(readOnly = true)
	public CartQuote quoteForCustomer(Long customerId) {
		Cart cart = cartService.findNonEmptyCart(customerId);
		return new CartQuote(customerId, quote(cartService.toPriceLines(cart), cart.getPromoCode()));
	}

	private PriceBreakdown withoutDiscount(List<PriceLine> lines, BigDecimal subtotal, String promoCode,
			PromoCodeStatus status) {
		BigDecimal zero = Money.of(BigDecimal.ZERO);
		return new PriceBreakdown(discountAllocator.allocate(lines, zero, subtotal), subtotal, zero, subtotal, promoCode,
				status);
	}

}
