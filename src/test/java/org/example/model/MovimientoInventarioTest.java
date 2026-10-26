package org.example.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MovimientoInventarioTest {

    private Producto producto() {
        return new Producto("Producto", "Desc", new BigDecimal("1000"), 10, Producto.EstadoProducto.ACTIVO);
    }

    @Test
    void rechazaCantidadCeroONegativa() {
        Producto producto = producto();
        assertThrows(MovimientoInventario.CantidadInvalidaException.class,
                () -> new MovimientoInventario(producto, 0, MovimientoInventario.TipoMovimiento.ENTRADA));
        assertThrows(MovimientoInventario.CantidadInvalidaException.class,
                () -> new MovimientoInventario(producto, -5, MovimientoInventario.TipoMovimiento.SALIDA));
    }

    @Test
    void rechazaCantidadNula() {
        Producto producto = producto();
        assertThrows(MovimientoInventario.CantidadInvalidaException.class,
                () -> new MovimientoInventario(producto, null, MovimientoInventario.TipoMovimiento.ENTRADA));
    }

    @Test
    void creaMovimientoDeEntradaConFecha() {
        Producto producto = producto();
        MovimientoInventario movimiento = MovimientoInventario.entrada(producto, 5);

        assertEquals(MovimientoInventario.TipoMovimiento.ENTRADA, movimiento.getTipo());
        assertEquals(5, movimiento.getCantidad());
        assertEquals(producto, movimiento.getProducto());
        assertNotNull(movimiento.getFecha());
    }

    @Test
    void creaMovimientoDeSalida() {
        Producto producto = producto();
        MovimientoInventario movimiento = MovimientoInventario.salida(producto, 3);

        assertEquals(MovimientoInventario.TipoMovimiento.SALIDA, movimiento.getTipo());
        assertEquals(3, movimiento.getCantidad());
        assertNotNull(movimiento.getFecha());
    }

    @Test
    void permiteActualizarCantidadProductoYFecha() {
        Producto producto = producto();
        MovimientoInventario movimiento = MovimientoInventario.entrada(producto, 1);

        movimiento.setCantidad(7);
        assertEquals(7, movimiento.getCantidad());

        Producto otro = new Producto("Otro", null, new BigDecimal("500"), 1, Producto.EstadoProducto.ACTIVO);
        movimiento.setProducto(otro);
        assertEquals(otro, movimiento.getProducto());

        var nuevaFecha = movimiento.getFecha().plusDays(1);
        movimiento.setFecha(nuevaFecha);
        assertEquals(nuevaFecha, movimiento.getFecha());
    }

    @Test
    void setCantidadRechazaValoresInvalidos() {
        MovimientoInventario movimiento = MovimientoInventario.entrada(producto(), 1);
        assertThrows(MovimientoInventario.CantidadInvalidaException.class, () -> movimiento.setCantidad(0));
        assertThrows(MovimientoInventario.CantidadInvalidaException.class, () -> movimiento.setCantidad(null));
    }
}
