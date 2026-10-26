package org.example.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank String nombre,
        String descripcion,
        @NotNull BigDecimal precio,
        @NotNull @Min(0) Integer stock,
        String estado
) {
}
