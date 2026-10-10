package com.shopflow.orders.catalog;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProductDto(Long id, String sku, String name, BigDecimal price, boolean active) implements Serializable {

	static ProductDto from(Product product) {
		return new ProductDto(product.getId(), product.getSku(), product.getName(), product.getPrice(),
				product.isActive());
	}

}
