package org.example.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductoTest {

    @Test
    void registrarProductoValido() {
        Producto producto = new Producto(
                "Laptop", "Laptop gamer",
                new BigDecimal("120000"), 10, Producto.EstadoProducto.ACTIVO);

        assertEquals("Laptop", producto.getNombre());
        assertEquals(10, producto.getStock());
        assertEquals(Producto.EstadoProducto.ACTIVO, producto.getEstado());
    }

    @Test
    void rechazaPrecioCero() {
        assertThrows(Producto.PrecioInvalidoException.class,
                () -> new Producto("X", null, BigDecimal.ZERO, 5, Producto.EstadoProducto.ACTIVO));
    }

    @Test
    void rechazaPrecioNegativo() {
        assertThrows(Producto.PrecioInvalidoException.class,
                () -> new Producto("X", null, new BigDecimal("-5000"), 5, Producto.EstadoProducto.ACTIVO));
    }

    @Test
    void rechazaStockNegativo() {
        assertThrows(Producto.CantidadInvalidaException.class,
                () -> new Producto("X", null, new BigDecimal("10000"), -1, Producto.EstadoProducto.ACTIVO));
    }

    @Test
    void descontarStockReduceElStock() {
        Producto producto = productoConStock(10);

        producto.descontarStock(4);

        assertEquals(6, producto.getStock());
    }

    @Test
    void noVendeMasAllaDelStock() {
        Producto producto = productoConStock(2);

        assertThrows(Producto.StockInsuficienteException.class, () -> producto.descontarStock(5));
        assertEquals(2, producto.getStock());
    }

    @Test
    void rechazaCantidadCero() {
        Producto producto = productoConStock(5);

        assertThrows(Producto.CantidadInvalidaException.class, () -> producto.descontarStock(0));
    }

    @Test
    void rechazaCantidadNegativaAlReponer() {
        Producto producto = productoConStock(5);

        assertThrows(Producto.CantidadInvalidaException.class, () -> producto.reponerStock(-1));
    }

    @Test
    void noDescuentaStockDeProductoInactivo() {
        Producto producto = new Producto(
                "X", null, new BigDecimal("10000"), 5, Producto.EstadoProducto.INACTIVO);

        assertThrows(Producto.ProductoInactivoException.class, () -> producto.descontarStock(1));
    }

    @Test
    void reponerStockSuma() {
        Producto producto = productoConStock(5);

        producto.reponerStock(10);

        assertEquals(15, producto.getStock());
    }

    private Producto productoConStock(int stock) {
        return new Producto("X", null, new BigDecimal("10000"), stock, Producto.EstadoProducto.ACTIVO);
    }
}