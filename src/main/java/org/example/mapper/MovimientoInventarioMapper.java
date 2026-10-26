package org.example.mapper;

import org.example.dto.response.MovimientoInventarioResponse;
import org.example.model.MovimientoInventario;

public class MovimientoInventarioMapper {

    private MovimientoInventarioMapper() {
    }

    public static MovimientoInventarioResponse toResponse(MovimientoInventario movimiento) {
        if (movimiento == null) {
            return null;
        }
        Long productoId = movimiento.getProducto() != null ? movimiento.getProducto().getId() : null;
        String productoNombre = movimiento.getProducto() != null ? movimiento.getProducto().getNombre() : null;
        String tipo = movimiento.getTipo() != null ? movimiento.getTipo().name() : null;
        return new MovimientoInventarioResponse(
                movimiento.getId(),
                productoId,
                productoNombre,
                movimiento.getCantidad(),
                tipo,
                movimiento.getFecha()
        );
    }
}
