package org.example.service.impl;

import org.example.dto.request.EntradaInventarioRequest;
import org.example.dto.response.MovimientoInventarioResponse;
import org.example.exception.CantidadInvalidaException;
import org.example.exception.ProductoNoEncontradoException;
import org.example.mapper.MovimientoInventarioMapper;
import org.example.model.MovimientoInventario;
import org.example.model.Producto;
import org.example.repository.MovimientoInventarioRepository;
import org.example.repository.ProductoRepository;
import org.example.service.InventarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public InventarioServiceImpl(ProductoRepository productoRepository,
                                 MovimientoInventarioRepository movimientoInventarioRepository) {
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Override
    public MovimientoInventarioResponse registrarEntrada(EntradaInventarioRequest request) {
        if (request.cantidad() <= 0) {
            throw new CantidadInvalidaException("La cantidad debe ser mayor que cero");
        }
        Producto producto = productoRepository.findById(request.productoId())
                .orElseThrow(() -> new ProductoNoEncontradoException(request.productoId()));
        producto.reponerStock(request.cantidad());
        MovimientoInventario movimiento = MovimientoInventario.entrada(producto, request.cantidad());
        MovimientoInventario guardado = movimientoInventarioRepository.save(movimiento);
        productoRepository.save(producto);
        return MovimientoInventarioMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer consultarStock(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNoEncontradoException(productoId));
        return producto.getStock();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse> consultarMovimientos(Long productoId) {
        return movimientoInventarioRepository.findByProductoIdOrderByFechaDesc(productoId).stream()
                .map(MovimientoInventarioMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse> consultarTodosMovimientos() {
        return movimientoInventarioRepository.findAllByOrderByFechaDesc().stream()
                .map(MovimientoInventarioMapper::toResponse)
                .toList();
    }
}
