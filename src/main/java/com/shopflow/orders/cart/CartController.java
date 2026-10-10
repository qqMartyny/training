package com.shopflow.orders.cart;

import com.shopflow.orders.common.web.Headers;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}

	@GetMapping
	public CartView get(@RequestHeader(Headers.CUSTOMER_ID) Long customerId) {
		return cartService.getCart(customerId);
	}

	@PostMapping("/items")
	public CartView addItem(@RequestHeader(Headers.CUSTOMER_ID) Long customerId,
			@Valid @RequestBody AddItemRequest request) {
		return cartService.addItem(customerId, request);
	}

	@DeleteMapping("/items/{productId}")
	public CartView removeItem(@RequestHeader(Headers.CUSTOMER_ID) Long customerId, @PathVariable Long productId) {
		return cartService.removeItem(customerId, productId);
	}

	@PutMapping("/promo-code")
	public CartView applyPromoCode(@RequestHeader(Headers.CUSTOMER_ID) Long customerId,
			@Valid @RequestBody ApplyPromoCodeRequest request) {
		return cartService.applyPromoCode(customerId, request.code());
	}

}
