package com.shopflow.orders.cart;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CartTest {

	@Test
	void mergesQuantityWhenSameProductAddedTwice() {
		Cart cart = new Cart(42L);

		cart.addItem(1L, 1);
		cart.addItem(1L, 2);

		assertThat(cart.getItems()).hasSize(1);
		assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(3);
	}

	@Test
	void keepsDifferentProductsAsSeparateLines() {
		Cart cart = new Cart(42L);

		cart.addItem(1L, 1);
		cart.addItem(2L, 1);

		assertThat(cart.getItems()).extracting(CartItem::getProductId).containsExactly(1L, 2L);
	}

	@Test
	void removesProduct() {
		Cart cart = new Cart(42L);
		cart.addItem(1L, 1);
		cart.addItem(2L, 1);

		cart.removeItem(1L);

		assertThat(cart.getItems()).extracting(CartItem::getProductId).containsExactly(2L);
	}

}
