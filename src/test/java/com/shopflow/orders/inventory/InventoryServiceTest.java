package com.shopflow.orders.inventory;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InventoryServiceTest {

	private final StockRepository stockRepository = mock(StockRepository.class);

	private final InventoryService inventoryService = new InventoryService(stockRepository);

	@Test
	void reservesStock() {
		StockItem stock = new StockItem(1001L, 10);
		when(stockRepository.findById(1001L)).thenReturn(Optional.of(stock));

		inventoryService.reserve(1001L, 3);

		assertThat(stock.getAvailable()).isEqualTo(7);
	}

	@Test
	void failsWhenNotEnoughStock() {
		when(stockRepository.findById(1001L)).thenReturn(Optional.of(new StockItem(1001L, 2)));

		assertThatThrownBy(() -> inventoryService.reserve(1001L, 3)).isInstanceOf(OutOfStockException.class);
	}

	@Test
	void doesNotOversellUnderConcurrentReservations() {
		StockItem stock = new StockItem(1001L, 10);
		when(stockRepository.findById(1001L)).thenReturn(Optional.of(stock));

		int succeeded = 0;
		for (int buyer = 0; buyer < 30; buyer++) {
			try {
				inventoryService.reserve(1001L, 1);
				succeeded++;
			}
			catch (OutOfStockException ex) {
				// покупателю не хватило товара
			}
		}

		assertThat(succeeded).isEqualTo(10);
		assertThat(stock.getAvailable()).isZero();
	}

}
