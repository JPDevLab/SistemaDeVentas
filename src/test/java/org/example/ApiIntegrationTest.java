package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.repository.MovimientoInventarioRepository;
import org.example.repository.PedidoRepository;
import org.example.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        movimientoInventarioRepository.deleteAll();
        pedidoRepository.deleteAll();
        productoRepository.deleteAll();
    }

    private long crearProducto(String nombre, String precio, int stock, String estado) throws Exception {
        String json = estado == null
                ? String.format("{\"nombre\":\"%s\",\"precio\":%s,\"stock\":%d}", nombre, precio, stock)
                : String.format("{\"nombre\":\"%s\",\"precio\":%s,\"stock\":%d,\"estado\":\"%s\"}",
                nombre, precio, stock, estado);
        String respuesta = mockMvc.perform(post("/productos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("id").asLong();
    }

    private long crearPedido(String cliente, long productoId, int cantidad) throws Exception {
        String json = String.format("{\"cliente\":\"%s\",\"detalles\":[{\"productoId\":%d,\"cantidad\":%d}]}",
                cliente, productoId, cantidad);
        String respuesta = mockMvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("id").asLong();
    }

    @Test
    void creaYConsultaProducto() throws Exception {
        long id = crearProducto("Laptop", "50000", 10, null);

        mockMvc.perform(get("/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void actualizaProducto() throws Exception {
        long id = crearProducto("Viejo", "100", 1, null);

        mockMvc.perform(put("/productos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nuevo\",\"descripcion\":\"D\",\"precio\":250,\"stock\":4,\"estado\":\"INACTIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo"))
                .andExpect(jsonPath("$.stock").value(4))
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    void rechazaPrecioNoPositivo() throws Exception {
        mockMvc.perform(post("/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Malo\",\"precio\":0,\"stock\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PRECIO_INVALIDO"));
    }

    @Test
    void registraEntradaYConsultaStock() throws Exception {
        long id = crearProducto("Producto", "100", 0, null);

        mockMvc.perform(post("/inventario/entradas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"productoId\":%d,\"cantidad\":5}", id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ENTRADA"));

        mockMvc.perform(get("/inventario/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void aplicaDescuentoEnFronteraDeCienMil() throws Exception {
        long id = crearProducto("Prod100000", "100000", 5, null);

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"cliente\":\"Frontera\",\"detalles\":[{\"productoId\":%d,\"cantidad\":1}]}", id)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(100000.00))
                .andExpect(jsonPath("$.descuento").value(5000.00))
                .andExpect(jsonPath("$.totalFinal").value(95000.00));
    }

    @Test
    void rechazaPedidoPorStockInsuficiente() throws Exception {
        long id = crearProducto("Escaso", "1000", 1, null);

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"cliente\":\"C\",\"detalles\":[{\"productoId\":%d,\"cantidad\":2}]}", id)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("STOCK_INSUFICIENTE"));
    }

    @Test
    void rechazaPedidoConProductoInactivo() throws Exception {
        long id = crearProducto("Inactivo", "1000", 5, "INACTIVO");

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"cliente\":\"C\",\"detalles\":[{\"productoId\":%d,\"cantidad\":1}]}", id)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("PRODUCTO_INACTIVO"));
    }

    @Test
    void rechazaPedidoSinProductos() throws Exception {
        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cliente\":\"C\",\"detalles\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("PEDIDO_SIN_PRODUCTOS"));
    }

    @Test
    void entregaYNoPermiteCancelarPedidoEntregado() throws Exception {
        long productoId = crearProducto("Producto", "50000", 5, null);
        long pedidoId = crearPedido("Cliente", productoId, 1);

        mockMvc.perform(patch("/pedidos/{id}/entregar", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));

        mockMvc.perform(patch("/pedidos/{id}/cancelar", pedidoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("PEDIDO_NO_CANCELABLE"));
    }

    @Test
    void cancelarRestauraStock() throws Exception {
        long productoId = crearProducto("Producto", "50000", 5, null);
        long pedidoId = crearPedido("Cliente", productoId, 2);

        mockMvc.perform(get("/inventario/{id}", productoId)).andExpect(content().string("3"));

        mockMvc.perform(patch("/pedidos/{id}/cancelar", pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));

        mockMvc.perform(get("/inventario/{id}", productoId)).andExpect(content().string("5"));
    }

    @Test
    void consultaPedidosPorCliente() throws Exception {
        long productoId = crearProducto("Producto", "1000", 10, null);
        crearPedido("Ana", productoId, 1);
        crearPedido("Luis", productoId, 1);

        mockMvc.perform(get("/pedidos").param("cliente", "Ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cliente").value("Ana"));
    }
}
