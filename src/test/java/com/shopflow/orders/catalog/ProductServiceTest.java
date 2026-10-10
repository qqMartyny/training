package com.shopflow.orders.catalog;

import java.math.BigDecimal;
import java.util.Optional;

import com.shopflow.orders.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceTest {

	private final ProductRepository productRepository = mock(ProductRepository.class);

	private final ProductService productService = new ProductService(productRepository);

	@Test
	void returnsProduct() {
		Product product = new Product("KB-1", "Keyboard", new BigDecimal("7990.00"));
		when(productRepository.findById(1001L)).thenReturn(Optional.of(product));

		assertThat(productService.getProduct(1001L).getName()).isEqualTo("Keyboard");
	}

	@Test
	void failsForUnknownProduct() {
		when(productRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productService.getProduct(1L)).isInstanceOf(NotFoundException.class);
	}

	void changePriceEvictsCachedProduct() {
		Product product = new Product("KB-1", "Keyboard", new BigDecimal("7990.00"));
		when(productRepository.findById(1001L)).thenReturn(Optional.of(product));
		productService.getProduct(1001L);

		productService.changePrice(1001L, new BigDecimal("6990.00"));

		assertThat(productService.getProduct(1001L).getPrice()).isEqualByComparingTo("6990.00");
	}

}
