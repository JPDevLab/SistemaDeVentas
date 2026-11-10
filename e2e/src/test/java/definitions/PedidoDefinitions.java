package definitions;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import net.thucydides.core.annotations.Steps;
import steps.PedidoSteps;

import java.util.Locale;

public class PedidoDefinitions {

    @Steps
    PedidoSteps pedidoSteps;

    @Dado("el producto {string} tiene stock {int}")
    public void verificarStockInicial(String producto, int stock) {
        pedidoSteps.verificarStockInicial(producto, stock);
    }

    @Cuando("crea un pedido para {string} con {int} unidades de {string}")
    public void crearPedido(String cliente, int cantidad, String producto) {
        pedidoSteps.crearPedido(cliente, producto, cantidad);
    }

    @Entonces("el pedido debe quedar en estado {string}")
    public void verificarEstado(String estado) {
        pedidoSteps.verificarEstado(estado);
    }

    @Y("el total final del pedido debe ser {int} pesos")
    public void verificarTotal(int total) {
        String totalFormateado = String.format(Locale.US, "%,d", total).replace(",", ".");
        pedidoSteps.verificarTotalFinal(totalFormateado);
    }

    @Y("el stock de {string} debe ser {int}")
    public void verificarStock(String producto, int stock) {
        pedidoSteps.verificarStockProducto(producto, stock);
    }

    @Cuando("cancela el pedido de {string}")
    public void cancelarPedido(String cliente) {
        pedidoSteps.cancelarPedido(cliente);
    }
}
