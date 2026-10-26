package org.example.service.impl;

import org.example.dto.request.ProductoRequest;
import org.example.dto.response.ProductoResponse;
import org.example.exception.PrecioInvalidoException;
import org.example.exception.ProductoNoEncontradoException;
import org.example.mapper.ProductoMapper;
import org.example.model.Producto;
import org.example.repository.ProductoRepository;
import org.example.service.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public ProductoResponse crear(ProductoRequest request) {
        if (request.precio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PrecioInvalidoException("El precio debe ser mayor que cero");
        }
        if (request.stock() < 0) {
            throw new org.example.exception.CantidadInvalidaException("El stock no puede ser negativo");
        }
        Producto producto = ProductoMapper.toEntity(request);
        Producto guardado = productoRepository.save(producto);
        return ProductoMapper.toResponse(guardado);
    }

    @Override
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
        if (request.precio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PrecioInvalidoException("El precio debe ser mayor que cero");
        }
        if (request.stock() < 0) {
            throw new org.example.exception.CantidadInvalidaException("El stock no puede ser negativo");
        }
        ProductoMapper.updateEntity(producto, request);
        Producto guardado = productoRepository.save(producto);
        return ProductoMapper.toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        return productoRepository.findAll().stream()
                .map(ProductoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNoEncontradoException(id));
        return ProductoMapper.toResponse(producto);
    }
}
