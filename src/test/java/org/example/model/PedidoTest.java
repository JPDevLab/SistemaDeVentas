package org.example.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoTest {

    @Test
    void calculaDescuentoYTotalFinalEnLosValoresFrontera() {
        Map<BigDecimal, BigDecimal[]> casos = new LinkedHashMap<>();
        casos.put(new BigDecimal("99999"), new BigDecimal[] {new BigDecimal("0"), new BigDecimal("99999.00")});
        casos.put(new BigDecimal("100000"), new BigDecimal[] {new BigDecimal("0.05"), new BigDecimal("95000.00")});
        casos.put(new BigDecimal("299999"), new BigDecimal[] {new BigDecimal("0.05"), new BigDecimal("284999.05")});
        casos.put(new BigDecimal("300000"), new BigDecimal[] {new BigDecimal("0.10"), new BigDecimal("270000.00")});
        casos.put(new BigDecimal("499999"), new BigDecimal[] {new BigDecimal("0.10"), new BigDecimal("449999.10")});
        casos.put(new BigDecimal("500000"), new BigDecimal[] {new BigDecimal("0.15"), new BigDecimal("425000.00")});

        for (Map.Entry<BigDecimal, BigDecimal[]> caso : casos.entrySet()) {
            Pedido pedido = pedidoConTotal(caso.getKey());
            BigDecimal descuentoEsperado = caso.getValue()[0];
            BigDecimal totalFinalEsperado = caso.getValue()[1];

            assertEquals(0, descuentoEsperado.compareTo(pedido.calcularPorcentajeDescuento()),
                    "Porcentaje incorrecto para total " + caso.getKey());
            assertEquals(0, totalFinalEsperado.compareTo(pedido.getTotalFinal()),
                    "Total final incorrecto para total " + caso.getKey());
        }
    }

    @Test
    void noPermiteCancelarUnPedidoEntregado() {
        Pedido pedido = new Pedido("Cliente", null);
        pedido.agregarDetalle(1L, "Producto", 1, new BigDecimal("100000"));
        pedido.marcarPagado();
        pedido.marcarEntregado();

        assertThrows(Pedido.EstadoInvalidoException.class, pedido::cancelar);
        assertEquals(Pedido.EstadoPedido.ENTREGADO, pedido.getEstado());
    }

    private Pedido pedidoConTotal(BigDecimal total) {
        Pedido pedido = new Pedido("Cliente", null);
        pedido.agregarDetalle(1L, "Producto", 1, total);
        return pedido;
    }
}