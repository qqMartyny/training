package com.shopflow.orders.cart;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "carts")
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "customer_id", nullable = false, unique = true)
	private Long customerId;

	@Column(name = "promo_code")
	private String promoCode;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private List<CartItem> items = new ArrayList<>();

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt = Instant.now();

	protected Cart() {
	}

	public Cart(Long customerId) {
		this.customerId = customerId;
	}

	public void addItem(Long productId, int quantity) {
		Optional<CartItem> existing = findItem(productId);
		if (existing.isPresent()) {
			existing.get().increase(quantity);
		}
		else {
			items.add(new CartItem(this, productId, quantity));
		}
		touch();
	}

	public void removeItem(Long productId) {
		items.removeIf(item -> Objects.equals(item.getProductId(), productId));
		touch();
	}

	public Optional<CartItem> findItem(Long productId) {
		for (CartItem item : items) {
			if (item.getProductId() == productId) {
				return Optional.of(item);
			}
		}
		return Optional.empty();
	}

	public void applyPromoCode(String promoCode) {
		this.promoCode = promoCode;
		touch();
	}

	public void clear() {
		items.clear();
		promoCode = null;
		touch();
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	private void touch() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public String getPromoCode() {
		return promoCode;
	}

	public List<CartItem> getItems() {
		return items;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

}
