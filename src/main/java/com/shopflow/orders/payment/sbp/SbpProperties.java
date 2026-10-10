package com.shopflow.orders.payment.sbp;

import jakarta.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("shopflow.payment.sbp")
public record SbpProperties(@NotBlank String merchantId, @NotBlank String qrBaseUrl) {
}
