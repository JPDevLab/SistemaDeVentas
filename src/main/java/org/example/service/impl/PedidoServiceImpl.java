package org.example.service.impl;

import org.example.dto.request.DetallePedidoRequest;
import org.example.dto.request.PedidoRequest;
import org.example.dto.response.PedidoResponse;
import org.example.exception.*;
import org.example.mapper.PedidoMapper;
import org.example.model.MovimientoInventario;
import org.example.model.Pedido;
import org.example.model.Producto;
import org.example.repository.MovimientoInventarioRepository;
import org.example.repository.PedidoRepository;
import org.example.repository.ProductoRepository;
import org.example.service.PedidoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Bogota");

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                             ProductoRepository productoRepository,
                             MovimientoInventarioRepository movimientoInventarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Override
    public PedidoResponse crear(PedidoRequest request) {
        if (request.detalles() == null || request.detalles().isEmpty()) {
            throw new PedidoSinProductosException("Un pedido debe contener al menos un producto");
        }

        Pedido pedido = new Pedido(request.cliente(), LocalDateTime.now(ZONA_HORARIA));
        List<DetalleConProducto> detallesConProducto = new ArrayList<>();

        // Adquirir bloqueo pesimista para cada producto
        for (DetallePedidoRequest detalleReq : request.detalles()) {
            if (detalleReq.cantidad() <= 0) {
                throw new CantidadInvalidaException("La cantidad debe ser mayor que cero");
            }
            Producto producto = productoRepository.findByIdForUpdate(detalleReq.productoId())
                    .orElseThrow(() -> new ProductoNoEncontradoException(detalleReq.productoId()));
            if (producto.getEstado() != Producto.EstadoProducto.ACTIVO) {
                throw new ProductoInactivoException("El producto " + producto.getNombre() + " está inactivo");
            }
            if (producto.getStock() < detalleReq.cantidad()) {
                throw new StockInsuficienteException("No existe inventario suficiente para completar la compra");
            }
            detallesConProducto.add(new DetalleConProducto(producto, detalleReq.cantidad()));
        }

        // Agregar detalles al pedido
        for (DetalleConProducto dc : detallesConProducto) {
            pedido.agregarDetalle(dc.producto.getId(), dc.producto.getNombre(), dc.cantidad, dc.producto.getPrecio());
        }

        // Descontar inventario y crear movimientos SALIDA
        for (DetalleConProducto dc : detallesConProducto) {
            dc.producto.descontarStock(dc.cantidad);
            productoRepository.save(dc.producto);
            MovimientoInventario movimiento = MovimientoInventario.salida(dc.producto, dc.cantidad);
            movimientoInventarioRepository.save(movimiento);
        }

        Pedido guardado = pedidoRepository.save(pedido);
        return PedidoMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(String cliente) {
        List<Pedido> pedidos;
        if (cliente != null && !cliente.isBlank()) {
            pedidos = pedidoRepository.findByClienteIgnoreCase(cliente);
        } else {
            pedidos = pedidoRepository.findAll();
        }
        return pedidos.stream()
                .map(PedidoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse obtener(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNoEncontradoException(id));
        return PedidoMapper.toResponse(pedido);
    }

    @Override
    public PedidoResponse cancelar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNoEncontradoException(id));
        if (pedido.getEstado() == Pedido.EstadoPedido.ENTREGADO) {
            throw new PedidoNoCancelableException("Un pedido entregado no puede cancelarse");
        }
        if (pedido.getEstado() == Pedido.EstadoPedido.CANCELADO) {
            // Idempotente - ya está cancelado
            return PedidoMapper.toResponse(pedido);
        }

        // Restaurar inventario por cada detalle
        if (pedido.getDetalles() != null) {
            for (Pedido.Detalle detalle : pedido.getDetalles()) {
                Producto producto = productoRepository.findById(detalle.getProductoId())
                        .orElse(null);
                if (producto != null) {
                    producto.reponerStock(detalle.getCantidad());
                    productoRepository.save(producto);
                    MovimientoInventario movimiento = MovimientoInventario.entrada(producto, detalle.getCantidad());
                    movimientoInventarioRepository.save(movimiento);
                }
            }
        }

        pedido.cancelar();
        Pedido guardado = pedidoRepository.save(pedido);
        return PedidoMapper.toResponse(guardado);
    }

    @Override
    public PedidoResponse entregar(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNoEncontradoException(id));
        if (pedido.getEstado() == Pedido.EstadoPedido.CANCELADO) {
            throw new org.example.model.Pedido.EstadoInvalidoException("No se puede entregar un pedido cancelado");
        }
        if (pedido.getEstado() == Pedido.EstadoPedido.ENTREGADO) {
            return PedidoMapper.toResponse(pedido);
        }
        if (pedido.getEstado() == Pedido.EstadoPedido.CREADO) {
            pedido.marcarPagado();
        }
        pedido.marcarEntregado();
        Pedido guardado = pedidoRepository.save(pedido);
        return PedidoMapper.toResponse(guardado);
    }

    private static class DetalleConProducto {
        final Producto producto;
        final int cantidad;

        DetalleConProducto(Producto producto, int cantidad) {
            this.producto = producto;
            this.cantidad = cantidad;
        }
    }
}
