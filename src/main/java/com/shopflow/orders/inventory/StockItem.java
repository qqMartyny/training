package com.shopflow.orders.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_items")
public class StockItem {

	@Id
	@Column(name = "product_id")
	private Long productId;

	@Column(nullable = false)
	private int available;

	@Column(nullable = false)
	private long version;

	protected StockItem() {
	}

	public StockItem(Long productId, int available) {
		this.productId = productId;
		this.available = available;
	}

	public Long getProductId() {
		return productId;
	}

	public int getAvailable() {
		return available;
	}

	public void setAvailable(int available) {
		this.available = available;
	}

}
