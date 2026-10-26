package org.example.service.impl;

import org.example.dto.request.ProductoRequest;
import org.example.dto.response.ProductoResponse;
import org.example.exception.CantidadInvalidaException;
import org.example.exception.PrecioInvalidoException;
import org.example.exception.ProductoNoEncontradoException;
import org.example.model.Producto;
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
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    @Test
    void crearRechazaPrecioNoPositivo() {
        ProductoRequest request = new ProductoRequest("P", "D", BigDecimal.ZERO, 1, null);

        assertThrows(PrecioInvalidoException.class, () -> productoService.crear(request));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void crearRechazaStockNegativo() {
        ProductoRequest request = new ProductoRequest("P", "D", new BigDecimal("100"), -1, null);

        assertThrows(CantidadInvalidaException.class, () -> productoService.crear(request));
        verify(productoRepository, never()).save(any());
    }

    @Test
    void crearGuardaProductoActivoPorDefecto() {
        ProductoRequest request = new ProductoRequest("P", "D", new BigDecimal("100"), 5, null);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductoResponse response = productoService.crear(request);

        assertEquals("P", response.nombre());
        assertEquals(0, new BigDecimal("100").compareTo(response.precio()));
        assertEquals(5, response.stock());
        assertEquals("ACTIVO", response.estado());
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void actualizarRechazaProductoInexistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        ProductoRequest request = new ProductoRequest("P", "D", new BigDecimal("100"), 1, null);

        assertThrows(ProductoNoEncontradoException.class, () -> productoService.actualizar(99L, request));
    }

    @Test
    void actualizarRechazaPrecioNoPositivo() {
        Producto producto = new Producto("P", "D", new BigDecimal("100"), 1, Producto.EstadoProducto.ACTIVO);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        ProductoRequest request = new ProductoRequest("P", "D", BigDecimal.ZERO, 1, null);

        assertThrows(PrecioInvalidoException.class, () -> productoService.actualizar(1L, request));
    }

    @Test
    void actualizaCamposDelProducto() {
        Producto producto = new Producto("Viejo", "D", new BigDecimal("100"), 1, Producto.EstadoProducto.ACTIVO);
        ReflectionTestUtils.setField(producto, "id", 1L);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        ProductoRequest request = new ProductoRequest("Nuevo", "Nueva", new BigDecimal("250"), 4, "INACTIVO");

        ProductoResponse response = productoService.actualizar(1L, request);

        assertEquals(1L, response.id());
        assertEquals("Nuevo", response.nombre());
        assertEquals(4, response.stock());
        assertEquals("INACTIVO", response.estado());
    }

    @Test
    void listarMapeaTodosLosProductos() {
        when(productoRepository.findAll()).thenReturn(List.of(
                new Producto("A", null, new BigDecimal("10"), 1, Producto.EstadoProducto.ACTIVO),
                new Producto("B", null, new BigDecimal("20"), 2, Producto.EstadoProducto.INACTIVO)));

        List<ProductoResponse> response = productoService.listar();

        assertEquals(2, response.size());
        assertEquals("A", response.get(0).nombre());
    }

    @Test
    void obtenerRechazaProductoInexistente() {
        when(productoRepository.findById(7L)).thenReturn(Optional.empty());
        assertThrows(ProductoNoEncontradoException.class, () -> productoService.obtener(7L));
    }

    @Test
    void obtenerDevuelveProductoExistente() {
        Producto producto = new Producto("A", null, new BigDecimal("10"), 1, Producto.EstadoProducto.ACTIVO);
        ReflectionTestUtils.setField(producto, "id", 7L);
        when(productoRepository.findById(7L)).thenReturn(Optional.of(producto));

        ProductoResponse response = productoService.obtener(7L);

        assertEquals(7L, response.id());
        assertEquals("A", response.nombre());
    }
}
