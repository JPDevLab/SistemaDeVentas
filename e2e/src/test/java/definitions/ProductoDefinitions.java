package definitions;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import net.thucydides.core.annotations.Steps;
import steps.ProductoSteps;

public class ProductoDefinitions {

    @Steps
    ProductoSteps productoSteps;

    @Cuando("crea un producto E2E con precio {int} y stock {int}")
    public void crearProducto(int precio, int stock) {
        productoSteps.crearProductoE2E(String.valueOf(precio), stock);
    }

    @Entonces("el producto E2E debe aparecer en el catálogo con stock {int}")
    public void verificarProducto(int stock) {
        productoSteps.verificarProductoConStock(stock);
    }

    @Cuando("registra una entrada de {int} unidades para el producto E2E")
    public void registrarEntrada(int cantidad) {
        productoSteps.registrarEntrada(cantidad);
    }

    @Entonces("el stock del producto E2E debe ser {int}")
    public void verificarStock(int stock) {
        productoSteps.verificarStockProducto(stock);
    }

    @Y("debe existir un movimiento de entrada de {int} unidades para el producto E2E")
    public void verificarMovimiento(int cantidad) {
        productoSteps.verificarMovimientoEntrada(cantidad);
    }
}
