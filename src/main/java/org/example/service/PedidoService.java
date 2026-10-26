package org.example.service;

import org.example.dto.request.PedidoRequest;
import org.example.dto.response.PedidoResponse;

import java.util.List;

public interface PedidoService {
    PedidoResponse crear(PedidoRequest request);
    List<PedidoResponse> listar(String cliente);
    PedidoResponse obtener(Long id);
    PedidoResponse cancelar(Long id);
    PedidoResponse entregar(Long id);
}
