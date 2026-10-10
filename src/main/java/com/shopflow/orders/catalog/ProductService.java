package com.shopflow.orders.catalog;

import java.math.BigDecimal;
import java.util.List;

import com.shopflow.orders.common.exception.NotFoundException;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

	public static final String CACHE_NAME = "products";

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Transactional(readOnly = true)
	public List<Product> findActive() {
		return productRepository.findAllByActiveTrueOrderByIdAsc();
	}

	// SHOP-190: цены дёргаются на каждый пересчёт корзины, кэшируем в Redis
	@Cacheable(cacheNames = CACHE_NAME, key = "#id")
	@Transactional(readOnly = true)
	public Product getProduct(Long id) {
		return productRepository.findById(id).orElseThrow(() -> NotFoundException.of("Product", id));
	}

	@Transactional
	public Product changePrice(Long id, BigDecimal newPrice) {
		Product product = productRepository.findById(id).orElseThrow(() -> NotFoundException.of("Product", id));
		product.changePrice(newPrice);
		return product;
	}

}
