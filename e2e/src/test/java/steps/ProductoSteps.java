package steps;

import pages.InventarioPage;
import pages.ProductosPage;
import support.TestData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProductoSteps {

    ProductosPage productosPage;
    InventarioPage inventarioPage;
    private String nombreProducto;

    public void crearProductoE2E(String precio, int stock) {
        nombreProducto = TestData.nombreProducto();
        productosPage.abrir();
        productosPage.crearProducto(
                nombreProducto,
                "Producto creado por la prueba E2E",
                precio,
                stock
        );
    }

    public void verificarProductoConStock(int stock) {
        assertEquals(String.valueOf(stock), productosPage.stockDeProducto(nombreProducto));
    }

    public void registrarEntrada(int cantidad) {
        inventarioPage.abrir();
        inventarioPage.seleccionarProducto(nombreProducto);
        inventarioPage.registrarEntrada(cantidad);
    }

    public void verificarStockProducto(int stock) {
        inventarioPage.esperarStock(stock);
        assertEquals(String.valueOf(stock), inventarioPage.stockActual());
        productosPage.abrir();
        productosPage.esperarStock(nombreProducto, stock);
        assertEquals(String.valueOf(stock), productosPage.stockDeProducto(nombreProducto));
    }

    public void verificarMovimientoEntrada(int cantidad) {
        inventarioPage.abrir();
        inventarioPage.seleccionarProducto(nombreProducto);
        assertTrue(inventarioPage.existeMovimientoEntrada(nombreProducto, cantidad));
    }
}
