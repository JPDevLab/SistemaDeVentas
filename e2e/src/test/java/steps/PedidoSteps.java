package steps;

import pages.PedidosPage;
import pages.ProductosPage;
import support.TestData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PedidoSteps {

    PedidosPage pedidosPage;
    ProductosPage productosPage;
    private String cliente;

    public void verificarStockInicial(String producto, int stock) {
        productosPage.abrir();
        assertEquals(String.valueOf(stock), productosPage.stockDeProducto(producto));
    }

    public void crearPedido(String clienteBase, String producto, int cantidad) {
        cliente = TestData.cliente(clienteBase);
        pedidosPage.abrir();
        pedidosPage.crearPedido(cliente, producto, cantidad);
    }

    public void verificarEstado(String estado) {
        assertEquals(estado, pedidosPage.estadoDePedido(cliente));
    }

    public void verificarTotalFinal(String total) {
        pedidosPage.esperarTotalFinal(cliente, total);
        assertTrue(pedidosPage.totalFinalDePedido(cliente).contains(total));
    }

    public void verificarStockProducto(String producto, int stock) {
        productosPage.abrir();
        productosPage.esperarStock(producto, stock);
        assertEquals(String.valueOf(stock), productosPage.stockDeProducto(producto));
    }

    public void cancelarPedido(String clienteBase) {
        pedidosPage.abrir();
        pedidosPage.cancelarPedido(cliente);
    }

    public void verificarEstadoCancelado(String estado) {
        assertEquals(estado, pedidosPage.estadoDePedido(cliente));
    }
}
