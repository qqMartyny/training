package com.shopflow.orders.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.shopflow.orders.payment.PaymentMethod;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "orders")
public class Order {

	@Id
	private UUID id;

	@Column(name = "customer_id", nullable = false)
	private Long customerId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false)
	private PaymentMethod paymentMethod;

	@Column(name = "promo_code")
	private String promoCode;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotal;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal discount;

	@Column(name = "payment_fee", nullable = false, precision = 12, scale = 2)
	private BigDecimal paymentFee;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private List<OrderItem> items = new ArrayList<>();

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	private long version;

	protected Order() {
	}

	public Order(Long customerId, PaymentMethod paymentMethod, String promoCode, BigDecimal subtotal,
			BigDecimal discount, BigDecimal paymentFee) {
		this.id = UUID.randomUUID();
		this.customerId = customerId;
		this.status = OrderStatus.NEW;
		this.paymentMethod = paymentMethod;
		this.promoCode = promoCode;
		this.subtotal = subtotal;
		this.discount = discount;
		this.paymentFee = paymentFee;
		this.total = subtotal.subtract(discount).add(paymentFee);
		this.createdAt = Instant.now();
		this.updatedAt = this.createdAt;
	}

	public void addItem(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal discount) {
		items.add(new OrderItem(this, productId, productName, unitPrice, quantity, discount));
	}

	/**
	 * @return предыдущий статус
	 */
	public OrderStatus changeStatus(OrderStatus target) {
		if (!status.canTransitionTo(target)) {
			throw new IllegalStatusTransitionException(status, target);
		}
		OrderStatus previous = status;
		status = target;
		updatedAt = Instant.now();
		return previous;
	}

	public UUID getId() {
		return id;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public String getPromoCode() {
		return promoCode;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	public BigDecimal getPaymentFee() {
		return paymentFee;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

}
