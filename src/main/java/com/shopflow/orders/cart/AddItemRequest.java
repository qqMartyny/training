package com.shopflow.orders.cart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddItemRequest(@NotNull Long productId, @Min(1) @Max(99) int quantity) {
}
