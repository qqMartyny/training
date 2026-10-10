package com.shopflow.orders.inventory;

import java.util.List;

import com.shopflow.orders.common.exception.NotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

	private final StockRepository stockRepository;

	public InventoryService(StockRepository stockRepository) {
		this.stockRepository = stockRepository;
	}

	@Transactional(readOnly = true)
	public int available(Long productId) {
		return stockRepository.findById(productId)
			.map(StockItem::getAvailable)
			.orElseThrow(() -> NotFoundException.of("Stock", productId));
	}

	/**
	 * Резервирует все позиции или ни одной: вызывается внутри транзакции оформления заказа.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public void reserve(List<StockLine> lines) {
		for (StockLine line : lines) {
			if (stockRepository.tryReserve(line.productId(), line.quantity()) == 0) {
				throw new OutOfStockException(line.productId());
			}
		}
	}

	@Transactional
	public void release(List<StockLine> lines) {
		for (StockLine line : lines) {
			stockRepository.release(line.productId(), line.quantity());
		}
	}

}
