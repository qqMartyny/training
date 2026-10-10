package com.shopflow.orders.inventory;

import com.shopflow.orders.common.exception.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

	private final StockRepository stockRepository;

	public InventoryService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	@Transactional(readOnly = true)
	public int available(Long productId) {
		return find(productId).getAvailable();
	}

	@Transactional
	public void reserve(Long productId, int quantity) {
		StockItem stock = find(productId);
		if (stock.getAvailable() < quantity) {
			throw new OutOfStockException(productId);
		}
		stock.setAvailable(stock.getAvailable() - quantity);
		stockRepository.save(stock);
	}

	@Transactional
	public void release(Long productId, int quantity) {
		StockItem stock = find(productId);
		stock.setAvailable(stock.getAvailable() + quantity);
		stockRepository.save(stock);
	}

	private StockItem find(Long productId) {
		return stockRepository.findById(productId).orElseThrow(() -> NotFoundException.of("Stock", productId));
	}

}
