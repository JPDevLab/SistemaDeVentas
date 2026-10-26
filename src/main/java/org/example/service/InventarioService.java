package org.example.service;

import org.example.dto.request.EntradaInventarioRequest;
import org.example.dto.response.MovimientoInventarioResponse;

import java.util.List;

public interface InventarioService {
    MovimientoInventarioResponse registrarEntrada(EntradaInventarioRequest request);
    Integer consultarStock(Long productoId);
    List<MovimientoInventarioResponse> consultarMovimientos(Long productoId);
    List<MovimientoInventarioResponse> consultarTodosMovimientos();
}
