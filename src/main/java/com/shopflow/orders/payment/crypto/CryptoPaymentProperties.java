package com.shopflow.orders.payment.crypto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param apiKey ключ API криптопроцессинга
 * @param currency код криптовалюты
 * @param rubRate курс: сколько рублей стоит 1 единица криптовалюты
 * @param feePercent сервисный сбор в процентах
 */
@Validated
@ConfigurationProperties("shopflow.payment.crypto")
public record CryptoPaymentProperties(@NotBlank String apiKey, @NotBlank String currency, @NotNull @Positive BigDecimal rubRate,
		@NotNull @PositiveOrZero BigDecimal feePercent) {
}
