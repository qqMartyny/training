package com.shopflow.orders.pricing;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "promo_codes")
public class PromoCode {

	@Id
	private String code;

	/**
	 * Тип скидки: процент или фиксированная сумма.
	 */
	@Column(nullable = false)
	private String type;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal value;

	@Column(name = "min_order_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal minOrderAmount = BigDecimal.ZERO;

	@Column(name = "valid_from", nullable = false)
	private Instant validFrom;

	@Column(name = "valid_to", nullable = false)
	private Instant validTo;

	@Column(nullable = false)
	private boolean active = true;

	protected PromoCode() {
	}

	public PromoCode(String code, String type, BigDecimal value, BigDecimal minOrderAmount, Instant validFrom,
			Instant validTo) {
		this.code = code;
		this.type = type;
		this.value = value;
		this.minOrderAmount = minOrderAmount;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	public boolean isValidAt(Instant moment) {
		return active && !moment.isBefore(validFrom) && moment.isBefore(validTo);
	}

	public String getCode() {
		return code;
	}

	public String getType() {
		return type;
	}

	public BigDecimal getValue() {
		return value;
	}

	public BigDecimal getMinOrderAmount() {
		return minOrderAmount;
	}

	public Instant getValidFrom() {
		return validFrom;
	}

	public Instant getValidTo() {
		return validTo;
	}

	public boolean isActive() {
		return active;
	}

}
