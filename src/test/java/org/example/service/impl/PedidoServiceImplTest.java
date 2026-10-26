package org.example.service.impl;

import org.example.dto.request.DetallePedidoRequest;
import org.example.dto.request.PedidoRequest;
import org.example.dto.response.PedidoResponse;
import org.example.exception.CantidadInvalidaException;
import org.example.exception.PedidoNoCancelableException;
import org.example.exception.PedidoNoEncontradoException;
import org.example.exception.PedidoSinProductosException;
import org.example.exception.ProductoInactivoException;
import org.example.exception.ProductoNoEncontradoException;
import org.example.exception.StockInsuficienteException;
import org.example.model.MovimientoInventario;
import org.example.model.Pedido;
import org.example.model.Producto;
import org.example.repository.MovimientoInventarioRepository;
import org.example.repository.PedidoRepository;
import org.example.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private Producto producto(long id, String precio, int stock, Producto.EstadoProducto estado) {
        Producto producto = new Producto("P" + id, "D", new BigDecimal(precio), stock, estado);
        ReflectionTestUtils.setField(producto, "id", id);
        return producto;
    }

    private Pedido pedidoCreado(long id, Pedido.EstadoPedido estado, long productoId, int cantidad, String precio) {
        Pedido pedido = new Pedido("Cliente", null);
        ReflectionTestUtils.setField(pedido, "id", id);
        pedido.agregarDetalle(productoId, "P" + productoId, cantidad, new BigDecimal(precio));
        if (estado == Pedido.EstadoPedido.PAGADO || estado == Pedido.EstadoPedido.ENTREGADO) {
            pedido.marcarPagado();
        }
        if (estado == Pedido.EstadoPedido.ENTREGADO) {
            pedido.marcarEntregado();
        }
        if (estado == Pedido.EstadoPedido.CANCELADO) {
            pedido.cancelar();
        }
        return pedido;
    }

    @Test
    void crearRechazaPedidoSinDetalles() {
        PedidoRequest request = new PedidoRequest("C", null);
        assertThrows(PedidoSinProductosException.class, () -> pedidoService.crear(request));

        PedidoRequest vacio = new PedidoRequest("C", List.of());
        assertThrows(PedidoSinProductosException.class, () -> pedidoService.crear(vacio));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void crearRechazaCantidadNoPositiva() {
        PedidoRequest request = new PedidoRequest("C", List.of(new DetallePedidoRequest(1L, 0)));
        assertThrows(CantidadInvalidaException.class, () -> pedidoService.crear(request));
    }

    @Test
    void crearRechazaProductoInexistente() {
        when(productoRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        PedidoRequest request = new PedidoRequest("C", List.of(new DetallePedidoRequest(1L, 1)));
        assertThrows(ProductoNoEncontradoException.class, () -> pedidoService.crear(request));
    }

    @Test
    void crearRechazaProductoInactivo() {
        when(productoRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(producto(1L, "1000", 5, Producto.EstadoProducto.INACTIVO)));
        PedidoRequest request = new PedidoRequest("C", List.of(new DetallePedidoRequest(1L, 1)));
        assertThrows(ProductoInactivoException.class, () -> pedidoService.crear(request));
    }

    @Test
    void crearRechazaStockInsuficiente() {
        when(productoRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(producto(1L, "1000", 2, Producto.EstadoProducto.ACTIVO)));
        PedidoRequest request = new PedidoRequest("C", List.of(new DetallePedidoRequest(1L, 3)));
        assertThrows(StockInsuficienteException.class, () -> pedidoService.crear(request));
        verify(movimientoInventarioRepository, never()).save(any());
    }

    @Test
    void crearCalculaDescuentoDescuentaStockYCreaSalida() {
        Producto producto = producto(1L, "100000", 5, Producto.EstadoProducto.ACTIVO);
        when(productoRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        PedidoRequest request = new PedidoRequest("Cliente", List.of(new DetallePedidoRequest(1L, 1)));

        PedidoResponse response = pedidoService.crear(request);

        assertEquals(0, new BigDecimal("100000").compareTo(response.total()));
        assertEquals(0, new BigDecimal("5000").compareTo(response.descuento()));
        assertEquals(0, new BigDecimal("95000").compareTo(response.totalFinal()));
        assertEquals("CREADO", response.estado());
        assertEquals(4, producto.getStock());

        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoInventarioRepository).save(captor.capture());
        assertEquals(MovimientoInventario.TipoMovimiento.SALIDA, captor.getValue().getTipo());
        assertEquals(1, captor.getValue().getCantidad());
    }

    @Test
    void cancelarRechazaPedidoInexistente() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(PedidoNoEncontradoException.class, () -> pedidoService.cancelar(99L));
    }

    @Test
    void cancelarRechazaPedidoEntregado() {
        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedidoCreado(1L, Pedido.EstadoPedido.ENTREGADO, 1L, 1, "1000")));

        assertThrows(PedidoNoCancelableException.class, () -> pedidoService.cancelar(1L));
    }

    @Test
    void cancelarRestauraStockYCreaEntrada() {
        Pedido pedido = pedidoCreado(1L, Pedido.EstadoPedido.CREADO, 2L, 2, "1000");
        Producto producto = producto(2L, "1000", 5, Producto.EstadoProducto.ACTIVO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse response = pedidoService.cancelar(1L);

        assertEquals("CANCELADO", response.estado());
        assertEquals(7, producto.getStock());
        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoInventarioRepository).save(captor.capture());
        assertEquals(MovimientoInventario.TipoMovimiento.ENTRADA, captor.getValue().getTipo());
        assertEquals(2, captor.getValue().getCantidad());
    }

    @Test
    void entregarDesdeCreadoCambiaEstado() {
        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedidoCreado(1L, Pedido.EstadoPedido.CREADO, 1L, 1, "1000")));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse response = pedidoService.entregar(1L);

        assertEquals("ENTREGADO", response.estado());
    }

    @Test
    void entregarRechazaPedidoCancelado() {
        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedidoCreado(1L, Pedido.EstadoPedido.CANCELADO, 1L, 1, "1000")));

        assertThrows(Pedido.EstadoInvalidoException.class, () -> pedidoService.entregar(1L));
    }

    @Test
    void listarFiltraPorClienteCuandoSeIndica() {
        Pedido pedido = pedidoCreado(1L, Pedido.EstadoPedido.CREADO, 1L, 1, "1000");
        when(pedidoRepository.findByClienteIgnoreCase("Cliente")).thenReturn(List.of(pedido));
        when(pedidoRepository.findAll()).thenReturn(List.of(pedido, pedidoCreado(2L, Pedido.EstadoPedido.CREADO, 1L, 1, "1000")));

        assertEquals(1, pedidoService.listar("Cliente").size());
        assertEquals(2, pedidoService.listar(null).size());
        assertEquals(2, pedidoService.listar("  ").size());
    }

    @Test
    void obtenerRechazaPedidoInexistente() {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(PedidoNoEncontradoException.class, () -> pedidoService.obtener(5L));
    }
}
