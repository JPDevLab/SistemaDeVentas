package org.example.dto.response;

import java.time.LocalDateTime;

public record MovimientoInventarioResponse(
        Long id,
        Long productoId,
        String productoNombre,
        Integer cantidad,
        String tipo,
        LocalDateTime fecha
) {
}
