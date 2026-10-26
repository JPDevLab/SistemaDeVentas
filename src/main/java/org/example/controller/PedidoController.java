package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.request.PedidoRequest;
import org.example.dto.response.PedidoResponse;
import org.example.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest request) {
        PedidoResponse response = pedidoService.crear(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listar(@RequestParam(value = "cliente", required = false) String cliente) {
        return ResponseEntity.ok(pedidoService.listar(cliente));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtener(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.cancelar(id));
    }

    @PatchMapping("/{id}/entregar")
    public ResponseEntity<PedidoResponse> entregar(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.entregar(id));
    }
}
