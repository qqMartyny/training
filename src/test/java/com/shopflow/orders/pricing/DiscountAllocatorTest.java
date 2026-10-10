package com.shopflow.orders.pricing;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountAllocatorTest {

	private final DiscountAllocator allocator = new DiscountAllocator();

	@Test
	void allocatesDiscountProportionallyToLineTotals() {
		List<PriceLine> lines = List.of(line("3000.00", 1), line("1000.00", 1));

		List<PricedLine> result = allocator.allocate(lines, new BigDecimal("400.00"), new BigDecimal("4000.00"));

		assertThat(result).extracting(PricedLine::discount)
			.usingElementComparator(BigDecimal::compareTo)
			.containsExactly(new BigDecimal("300.00"), new BigDecimal("100.00"));
	}

	@Test
	void sumOfLineDiscountsEqualsTotalDiscountForNonDivisibleAmounts() {
		List<PriceLine> lines = List.of(line("100.00", 1), line("100.00", 1), line("100.00", 1));

		List<PricedLine> result = allocator.allocate(lines, new BigDecimal("10.00"), new BigDecimal("300.00"));

		BigDecimal sum = result.stream().map(PricedLine::discount).reduce(BigDecimal.ZERO, BigDecimal::add);
		assertThat(sum).isEqualByComparingTo("10.00");
		assertThat(result).extracting(PricedLine::discount)
			.usingElementComparator(BigDecimal::compareTo)
			.containsExactly(new BigDecimal("3.33"), new BigDecimal("3.33"), new BigDecimal("3.34"));
	}

	@Test
	void lineDiscountNeverExceedsLineTotal() {
		List<PriceLine> lines = List.of(line("7990.00", 1), line("390.00", 3));

		List<PricedLine> result = allocator.allocate(lines, new BigDecimal("1000.00"), new BigDecimal("9160.00"));

		assertThat(result).allSatisfy(l -> assertThat(l.discount()).isLessThanOrEqualTo(l.lineTotal()));
	}

	private static PriceLine line(String price, int quantity) {
		return new PriceLine(1L, "p", new BigDecimal(price), quantity);
	}

}
