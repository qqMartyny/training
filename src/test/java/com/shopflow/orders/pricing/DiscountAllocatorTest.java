package com.shopflow.orders.pricing;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DiscountAllocatorTest {

	private final DiscountAllocator allocator = new DiscountAllocator();

	@Test
	void allocatesDiscountToEveryLine() {
		List<PriceLine> lines = List.of(line("3000.00", 1), line("1000.00", 1));

		List<PricedLine> result = allocator.allocate(lines, new BigDecimal("400.00"), new BigDecimal("4000.00"));

		assertNotNull(result);
		assertEquals(2, result.size());
	}

	@Test
	void returnsEmptyListForEmptyCart() {
		List<PricedLine> result = allocator.allocate(List.of(), BigDecimal.ZERO, BigDecimal.ZERO);

		assertEquals(0, result.size());
	}

	private static PriceLine line(String price, int quantity) {
		return new PriceLine(1L, "p", new BigDecimal(price), quantity);
	}

}
