package com.shopflow.orders.cart;

import java.util.List;

import com.shopflow.orders.catalog.ProductDto;
import com.shopflow.orders.catalog.ProductService;
import com.shopflow.orders.common.exception.BusinessException;
import com.shopflow.orders.pricing.PriceBreakdown;
import com.shopflow.orders.pricing.PriceLine;
import com.shopflow.orders.pricing.PricingService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

	private final CartRepository cartRepository;

	private final ProductService productService;

	private final PricingService pricingService;

	public CartService(CartRepository cartRepository, ProductService productService, PricingService pricingService) {
		this.cartRepository = cartRepository;
		this.productService = productService;
		this.pricingService = pricingService;
	}

	@Transactional(readOnly = true)
	public CartView getCart(Long customerId) {
		Cart cart = cartRepository.findByCustomerId(customerId).orElseGet(() -> new Cart(customerId));
		return CartView.of(customerId, price(cart));
	}

	@Transactional
	public CartView addItem(Long customerId, AddItemRequest request) {
		ProductDto product = productService.getProduct(request.productId());
		if (!product.active()) {
			throw new BusinessException("PRODUCT_NOT_AVAILABLE", "Product is not available: " + product.id());
		}
		Cart cart = findOrCreate(customerId);
		cart.addItem(product.id(), request.quantity());
		return CartView.of(customerId, price(cart));
	}

	@Transactional
	public CartView removeItem(Long customerId, Long productId) {
		Cart cart = findOrCreate(customerId);
		cart.removeItem(productId);
		return CartView.of(customerId, price(cart));
	}

	@Transactional
	public CartView applyPromoCode(Long customerId, String code) {
		Cart cart = findOrCreate(customerId);
		cart.applyPromoCode(code.trim().toUpperCase());
		PriceBreakdown breakdown = price(cart);
		if (breakdown.promoCodeRejected()) {
			throw new BusinessException("PROMO_CODE_" + breakdown.promoCodeStatus(), "Promo code cannot be applied: " + code);
		}
		return CartView.of(customerId, breakdown);
	}

	@Transactional(readOnly = true)
	public CartQuote quote(Long customerId) {
		Cart cart = cartRepository.findByCustomerId(customerId)
			.filter(c -> !c.isEmpty())
			.orElseThrow(() -> new BusinessException("CART_EMPTY", "Cart is empty"));
		return new CartQuote(customerId, price(cart));
	}

	@Transactional
	public void clear(Long customerId) {
		cartRepository.findByCustomerId(customerId).ifPresent(Cart::clear);
	}

	private Cart findOrCreate(Long customerId) {
		return cartRepository.findByCustomerId(customerId).orElseGet(() -> cartRepository.save(new Cart(customerId)));
	}

	private PriceBreakdown price(Cart cart) {
		List<PriceLine> lines = cart.getItems().stream().map(item -> {
			ProductDto product = productService.getProduct(item.getProductId());
			return new PriceLine(product.id(), product.name(), product.price(), item.getQuantity());
		}).toList();
		return pricingService.quote(lines, cart.getPromoCode());
	}

}
