package org.example.service;

import org.example.dto.request.ProductoRequest;
import org.example.dto.response.ProductoResponse;

import java.util.List;

public interface ProductoService {
    ProductoResponse crear(ProductoRequest request);
    ProductoResponse actualizar(Long id, ProductoRequest request);
    List<ProductoResponse> listar();
    ProductoResponse obtener(Long id);
}
