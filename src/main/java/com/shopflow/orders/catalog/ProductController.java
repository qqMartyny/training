package com.shopflow.orders.catalog;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	public List<Product> list() {
		return productService.findActive();
	}

	@GetMapping("/{id}")
	public Product get(@PathVariable Long id) {
		return productService.getProduct(id);
	}

	@PatchMapping("/{id}/price")
	public Product changePrice(@PathVariable Long id, @Valid @RequestBody ChangePriceRequest request) {
		return productService.changePrice(id, request.price());
	}

}
