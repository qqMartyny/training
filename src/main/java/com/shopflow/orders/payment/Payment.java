package com.shopflow.orders.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "payments")
public class Payment {

	@Id
	private UUID id;

	@Column(name = "order_id", nullable = false)
	private UUID orderId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentMethod method;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus status;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(name = "external_id")
	private String externalId;

	@Column(name = "sbp_qr_payload")
	private String sbpQrPayload;

	@Column(name = "crypto_wallet_address")
	private String cryptoWalletAddress;

	@Column(name = "crypto_amount", precision = 20, scale = 8)
	private BigDecimal cryptoAmount;

	@Column(name = "failure_reason")
	private String failureReason;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	private long version;

	protected Payment() {
	}

	public static Payment from(UUID orderId, PaymentMethod method, BigDecimal amount, PaymentResult result) {
		Payment payment = new Payment();
		payment.id = UUID.randomUUID();
		payment.orderId = orderId;
		payment.method = method;
		payment.amount = amount;
		payment.status = result.status();
		payment.externalId = result.externalId();
		payment.sbpQrPayload = result.qrPayload();
		payment.cryptoWalletAddress = result.cryptoAddress();
		payment.cryptoAmount = result.cryptoAmount();
		payment.failureReason = result.failureReason();
		payment.createdAt = Instant.now();
		payment.updatedAt = payment.createdAt;
		return payment;
	}

	public void complete(PaymentStatus finalStatus, String failureReason) {
		if (status.isFinal()) {
			throw new IllegalStateException("Payment " + id + " is already " + status);
		}
		this.status = finalStatus;
		this.failureReason = failureReason;
		this.updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public UUID getOrderId() {
		return orderId;
	}

	public PaymentMethod getMethod() {
		return method;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public String getExternalId() {
		return externalId;
	}

	public String getSbpQrPayload() {
		return sbpQrPayload;
	}

	public String getCryptoWalletAddress() {
		return cryptoWalletAddress;
	}

	public BigDecimal getCryptoAmount() {
		return cryptoAmount;
	}

	public String getFailureReason() {
		return failureReason;
	}

}
