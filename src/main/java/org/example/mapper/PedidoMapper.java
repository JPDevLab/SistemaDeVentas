package org.example.mapper;

import org.example.dto.response.DetallePedidoResponse;
import org.example.dto.response.PedidoResponse;
import org.example.model.Pedido;

import java.util.ArrayList;
import java.util.List;

public class PedidoMapper {

    private PedidoMapper() {
    }

    public static PedidoResponse toResponse(Pedido pedido) {
        if (pedido == null) {
            return null;
        }
        List<DetallePedidoResponse> detalles = new ArrayList<>();
        if (pedido.getDetalles() != null) {
            for (Pedido.Detalle detalle : pedido.getDetalles()) {
                detalles.add(new DetallePedidoResponse(
                        detalle.getProductoId(),
                        detalle.getNombreProducto(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                ));
            }
        }
        String estado = pedido.getEstado() != null ? pedido.getEstado().name() : null;
        return new PedidoResponse(
                pedido.getId(),
                pedido.getFecha(),
                pedido.getCliente(),
                pedido.getTotal(),
                pedido.getDescuento(),
                pedido.getTotalFinal(),
                estado,
                detalles
        );
    }
}
