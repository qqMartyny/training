package com.shopflow.orders.order;

import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(@NotNull OrderStatus status) {
}
