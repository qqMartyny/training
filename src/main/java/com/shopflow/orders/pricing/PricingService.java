package com.shopflow.orders.pricing;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricingService {

	private final PromoCodeRepository promoCodeRepository;

	private final DiscountAllocator discountAllocator;

	private final Map<DiscountType, DiscountPolicy> policies = new EnumMap<>(DiscountType.class);

	private final Clock clock;

	public PricingService(PromoCodeRepository promoCodeRepository, DiscountAllocator discountAllocator,
			List<DiscountPolicy> discountPolicies, Clock clock) {
		this.promoCodeRepository = promoCodeRepository;
		this.discountAllocator = discountAllocator;
		this.clock = clock;
		for (DiscountPolicy policy : discountPolicies) {
			if (policies.put(policy.type(), policy) != null) {
				throw new IllegalStateException("Duplicate DiscountPolicy for " + policy.type());
			}
		}
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

		DiscountPolicy policy = policies.get(promo.getType());
		if (policy == null) {
			throw new IllegalStateException("No DiscountPolicy registered for " + promo.getType());
		}
		BigDecimal discount = Money.of(policy.discount(subtotal, promo.getValue())).min(subtotal);
		BigDecimal total = subtotal.subtract(discount);
		return new PriceBreakdown(discountAllocator.allocate(lines, discount, subtotal), subtotal, discount, total,
				promo.getCode(), PromoCodeStatus.APPLIED);
	}

	private PriceBreakdown withoutDiscount(List<PriceLine> lines, BigDecimal subtotal, String promoCode,
			PromoCodeStatus status) {
		BigDecimal zero = Money.of(BigDecimal.ZERO);
		return new PriceBreakdown(discountAllocator.allocate(lines, zero, subtotal), subtotal, zero, subtotal, promoCode,
				status);
	}

}
