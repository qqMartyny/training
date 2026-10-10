package com.shopflow.orders.catalog;

import java.math.BigDecimal;
import java.util.List;

import com.shopflow.orders.common.exception.NotFoundException;

import org.springframework.cache.annotation.CacheEvict;
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
	public List<ProductDto> findActive() {
		return productRepository.findAllByActiveTrueOrderByIdAsc().stream().map(ProductDto::from).toList();
	}

	@Cacheable(cacheNames = CACHE_NAME, key = "#id")
	@Transactional(readOnly = true)
	public ProductDto getProduct(Long id) {
		return productRepository.findById(id).map(ProductDto::from).orElseThrow(() -> NotFoundException.of("Product", id));
	}

	@CacheEvict(cacheNames = CACHE_NAME, key = "#id")
	@Transactional
	public ProductDto changePrice(Long id, BigDecimal newPrice) {
		Product product = productRepository.findById(id).orElseThrow(() -> NotFoundException.of("Product", id));
		product.changePrice(newPrice);
		return ProductDto.from(product);
	}

}
