package org.example.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DetallePedidoRequest(
        @NotNull Long productoId,
        @NotNull @Positive Integer cantidad
) {
}
