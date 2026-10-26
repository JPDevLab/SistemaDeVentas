package org.example.service.impl;

import org.example.dto.request.EntradaInventarioRequest;
import org.example.dto.response.MovimientoInventarioResponse;
import org.example.exception.CantidadInvalidaException;
import org.example.exception.ProductoNoEncontradoException;
import org.example.model.MovimientoInventario;
import org.example.model.Producto;
import org.example.repository.MovimientoInventarioRepository;
import org.example.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Producto productoConId(long id, int stock) {
        Producto producto = new Producto("P", "D", new BigDecimal("100"), stock, Producto.EstadoProducto.ACTIVO);
        ReflectionTestUtils.setField(producto, "id", id);
        return producto;
    }

    @Test
    void registrarEntradaRechazaCantidadNoPositiva() {
        EntradaInventarioRequest request = new EntradaInventarioRequest(1L, 0);
        assertThrows(CantidadInvalidaException.class,
                () -> inventarioService.registrarEntrada(request));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void registrarEntradaRechazaProductoInexistente() {
        when(productoRepository.findById(5L)).thenReturn(Optional.empty());
        EntradaInventarioRequest request = new EntradaInventarioRequest(5L, 3);
        assertThrows(ProductoNoEncontradoException.class,
                () -> inventarioService.registrarEntrada(request));
    }

    @Test
    void registrarEntradaAumentaStockYCreaMovimiento() {
        Producto producto = productoConId(1L, 10);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MovimientoInventarioResponse response = inventarioService.registrarEntrada(new EntradaInventarioRequest(1L, 5));

        assertEquals(15, producto.getStock());
        assertEquals(5, response.cantidad());
        assertEquals("ENTRADA", response.tipo());
        assertEquals(1L, response.productoId());
        verify(productoRepository).save(producto);
        verify(movimientoInventarioRepository).save(any(MovimientoInventario.class));
    }

    @Test
    void consultarStockRechazaProductoInexistente() {
        when(productoRepository.findById(9L)).thenReturn(Optional.empty());
        assertThrows(ProductoNoEncontradoException.class, () -> inventarioService.consultarStock(9L));
    }

    @Test
    void consultarStockDevuelveStockActual() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoConId(1L, 8)));
        assertEquals(8, inventarioService.consultarStock(1L));
    }

    @Test
    void consultarMovimientosMapeaResultados() {
        Producto producto = productoConId(1L, 10);
        when(movimientoInventarioRepository.findByProductoIdOrderByFechaDesc(1L))
                .thenReturn(List.of(MovimientoInventario.entrada(producto, 2), MovimientoInventario.salida(producto, 1)));

        List<MovimientoInventarioResponse> respuesta = inventarioService.consultarMovimientos(1L);

        assertEquals(2, respuesta.size());
        assertEquals("ENTRADA", respuesta.get(0).tipo());
        assertEquals("SALIDA", respuesta.get(1).tipo());
    }

    @Test
    void consultarTodosMovimientosMapeaResultados() {
        Producto producto = productoConId(1L, 10);
        when(movimientoInventarioRepository.findAllByOrderByFechaDesc())
                .thenReturn(List.of(MovimientoInventario.salida(producto, 4)));

        List<MovimientoInventarioResponse> respuesta = inventarioService.consultarTodosMovimientos();

        assertEquals(1, respuesta.size());
        assertEquals(4, respuesta.get(0).cantidad());
    }
}
