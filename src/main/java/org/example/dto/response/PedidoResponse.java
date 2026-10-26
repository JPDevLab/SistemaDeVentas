package org.example.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        LocalDateTime fecha,
        String cliente,
        BigDecimal total,
        BigDecimal descuento,
        BigDecimal totalFinal,
        String estado,
        List<DetallePedidoResponse> detalles
) {
}
