package org.example.mapper;

import org.example.dto.request.ProductoRequest;
import org.example.dto.response.ProductoResponse;
import org.example.model.Producto;

public class ProductoMapper {

    private ProductoMapper() {
    }

    public static ProductoResponse toResponse(Producto producto) {
        if (producto == null) {
            return null;
        }
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getEstado() != null ? producto.getEstado().name() : null
        );
    }

    public static Producto toEntity(ProductoRequest request) {
        if (request == null) {
            return null;
        }
        Producto.EstadoProducto estado = Producto.EstadoProducto.ACTIVO;
        if (request.estado() != null && !request.estado().isBlank()) {
            try {
                estado = Producto.EstadoProducto.valueOf(request.estado().toUpperCase());
            } catch (Exception ignored) {
                estado = Producto.EstadoProducto.ACTIVO;
            }
        }
        return new Producto(request.nombre(), request.descripcion(), request.precio(), request.stock(), estado);
    }

    public static void updateEntity(Producto producto, ProductoRequest request) {
        if (producto == null || request == null) {
            return;
        }
        if (request.nombre() != null && !request.nombre().isBlank()) {
            producto.setNombre(request.nombre());
        }
        if (request.descripcion() != null) {
            producto.setDescripcion(request.descripcion());
        }
        producto.setPrecio(request.precio());
        producto.setStock(request.stock());
        if (request.estado() != null && !request.estado().isBlank()) {
            try {
                producto.setEstado(Producto.EstadoProducto.valueOf(request.estado().toUpperCase()));
            } catch (Exception ignored) {
                // No cambiar estado si es inválido
            }
        }
    }
}
