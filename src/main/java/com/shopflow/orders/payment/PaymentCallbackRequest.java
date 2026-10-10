package com.shopflow.orders.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Уведомление провайдера о финальном статусе платежа (СБП, крипто). Провайдер доставляет
 * уведомления по схеме at-least-once: одно и то же уведомление может прийти несколько раз.
 */
public record PaymentCallbackRequest(@NotBlank String externalId, @NotNull PaymentStatus status, String failureReason) {
}
