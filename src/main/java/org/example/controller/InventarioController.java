package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.request.EntradaInventarioRequest;
import org.example.dto.response.MovimientoInventarioResponse;
import org.example.service.InventarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @PostMapping("/inventario/entradas")
    public ResponseEntity<MovimientoInventarioResponse> registrarEntrada(@Valid @RequestBody EntradaInventarioRequest request) {
        MovimientoInventarioResponse response = inventarioService.registrarEntrada(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/inventario/{productoId}")
    public ResponseEntity<Integer> consultarStock(@PathVariable Long productoId) {
        Integer stock = inventarioService.consultarStock(productoId);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/inventario/movimientos")
    public ResponseEntity<List<MovimientoInventarioResponse>> consultarMovimientos(@RequestParam(value = "productoId", required = false) Long productoId) {
        if (productoId != null) {
            return ResponseEntity.ok(inventarioService.consultarMovimientos(productoId));
        }
        return ResponseEntity.ok(inventarioService.consultarTodosMovimientos());
    }
}
