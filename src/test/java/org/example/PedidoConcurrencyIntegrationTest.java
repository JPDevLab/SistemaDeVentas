package org.example;

import org.example.dto.request.DetallePedidoRequest;
import org.example.dto.request.PedidoRequest;
import org.example.exception.StockInsuficienteException;
import org.example.model.Producto;
import org.example.repository.MovimientoInventarioRepository;
import org.example.repository.PedidoRepository;
import org.example.repository.ProductoRepository;
import org.example.service.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class PedidoConcurrencyIntegrationTest {

    private static final int STOCK_INICIAL = 10;
    private static final int COMPRADORES = 20;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        movimientoInventarioRepository.deleteAll();
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();
    }

    @Test
    void diezDeVeinteComprasTienenExitoSinDejarStockNegativo() throws Exception {
        Producto producto = productoRepository.save(
                new Producto("Concurrente", "Prueba", new BigDecimal("1000"), STOCK_INICIAL,
                        Producto.EstadoProducto.ACTIVO));
        Long productoId = producto.getId();

        ExecutorService pool = Executors.newFixedThreadPool(COMPRADORES);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<String>> resultados = new ArrayList<>();

        for (int i = 0; i < COMPRADORES; i++) {
            final int numero = i;
            resultados.add(pool.submit(() -> {
                salida.await();
                try {
                    pedidoService.crear(new PedidoRequest("Cliente" + numero,
                            List.of(new DetallePedidoRequest(productoId, 1))));
                    return "OK";
                } catch (StockInsuficienteException ex) {
                    return "SIN_STOCK";
                } catch (Exception ex) {
                    return "ERROR:" + ex.getClass().getSimpleName() + ":" + ex.getMessage();
                }
            }));
        }

        salida.countDown();
        int exitosas = 0;
        int rechazadas = 0;
        List<String> errores = new ArrayList<>();
        for (Future<String> futuro : resultados) {
            String resultado = futuro.get(60, TimeUnit.SECONDS);
            switch (resultado) {
                case "OK" -> exitosas++;
                case "SIN_STOCK" -> rechazadas++;
                default -> errores.add(resultado);
            }
        }
        pool.shutdownNow();

        assertEquals(COMPRADORES - STOCK_INICIAL, rechazadas, "Compras rechazadas esperadas");
        assertEquals(STOCK_INICIAL, exitosas, "Compras exitosas esperadas");
        assertTrue(errores.isEmpty(), "Errores inesperados: " + errores);
        assertEquals(0, productoRepository.findById(productoId).orElseThrow().getStock(),
                "El stock final debe ser cero y nunca negativo");
    }
}
