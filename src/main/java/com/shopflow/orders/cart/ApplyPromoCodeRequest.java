package com.shopflow.orders.cart;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplyPromoCodeRequest(@NotBlank @Size(max = 32) String code) {
}
