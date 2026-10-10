package com.shopflow.orders.inventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.shopflow.orders.AbstractIntegrationTest;
import com.shopflow.orders.catalog.Product;
import com.shopflow.orders.catalog.ProductRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryConcurrencyIT extends AbstractIntegrationTest {

	@Autowired
	private InventoryService inventoryService;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Test
	void doesNotOversellUnderConcurrentReservations() throws Exception {
		Product product = productRepository
			.save(new Product("RACE-" + System.nanoTime(), "Limited edition", new BigDecimal("100.00")));
		stockRepository.save(new StockItem(product.getId(), 10));

		int buyers = 30;
		ExecutorService pool = Executors.newFixedThreadPool(buyers);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<Boolean>> results = new ArrayList<>();
		for (int i = 0; i < buyers; i++) {
			Callable<Boolean> buyer = () -> {
				start.await();
				try {
					transactionTemplate.executeWithoutResult(
							tx -> inventoryService.reserve(List.of(new StockLine(product.getId(), 1))));
					return true;
				}
				catch (RuntimeException ex) {
					return false;
				}
			};
			results.add(pool.submit(buyer));
		}
		start.countDown();

		int succeeded = 0;
		for (Future<Boolean> result : results) {
			if (result.get()) {
				succeeded++;
			}
		}
		pool.shutdown();

		assertThat(succeeded).isEqualTo(10);
		assertThat(inventoryService.available(product.getId())).isZero();
	}

}
