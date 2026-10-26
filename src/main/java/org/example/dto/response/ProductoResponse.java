package org.example.dto.response;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        String estado
) {
}
