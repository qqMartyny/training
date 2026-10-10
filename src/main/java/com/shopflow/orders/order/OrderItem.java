package com.shopflow.orders.order;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id")
	private Order order;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "product_name", nullable = false)
	private String productName;

	@Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
	private BigDecimal unitPrice;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal discount;

	protected OrderItem() {
	}

	OrderItem(Order order, Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal discount) {
		this.order = order;
		this.productId = productId;
		this.productName = productName;
		this.unitPrice = unitPrice;
		this.quantity = quantity;
		this.discount = discount;
	}

	public Long getId() {
		return id;
	}

	public Long getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public int getQuantity() {
		return quantity;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof OrderItem other)) {
			return false;
		}
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
