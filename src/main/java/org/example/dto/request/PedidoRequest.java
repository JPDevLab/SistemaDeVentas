package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PedidoRequest(
        @NotBlank String cliente,
        List<DetallePedidoRequest> detalles
) {
}
