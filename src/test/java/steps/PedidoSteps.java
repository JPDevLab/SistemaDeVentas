package steps;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import org.example.model.Pedido;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PedidoSteps {

    private Pedido pedido;
    private Exception excepcion;

    @Dado("un pedido con compra total de {long} pesos")
    public void unPedidoConCompra(Long total) {
        pedido = new Pedido("Cliente", null);
        pedido.agregarDetalle(1L, "Producto", 1, new BigDecimal(total));
    }

    @Cuando("se calcula el porcentaje de descuento")
    public void seCalculaElPorcentajeDeDescuento() {
        // No op: el cálculo se verifica en el paso Entonces
    }

    @Entonces("el descuento debe ser del {int} por ciento")
    public void elDescuentoDebeSerDel(int porcentaje) {
        BigDecimal esperado = new BigDecimal(porcentaje).movePointLeft(2);
        assertEquals(0, esperado.compareTo(pedido.calcularPorcentajeDescuento()));
    }

    @Y("el total final debe ser {long} pesos")
    public void elTotalFinalDebeSer(Long totalFinal) {
        assertEquals(0, new BigDecimal(totalFinal).compareTo(pedido.getTotalFinal()));
    }

    @Dado("un pedido entregado")
    public void unPedidoEntregado() {
        pedido = new Pedido("Cliente", null);
        pedido.agregarDetalle(1L, "Producto", 1, new BigDecimal("100000"));
        pedido.marcarPagado();
        pedido.marcarEntregado();
    }

    @Cuando("se intenta cancelar el pedido")
    public void seIntentaCancelarElPedido() {
        try {
            pedido.cancelar();
        } catch (Exception e) {
            excepcion = e;
        }
    }

    @Entonces("el sistema debe indicar que un pedido entregado no puede cancelarse")
    public void elSistemaIndicaQueUnPedidoEntregadoNoPuedeCancelarse() {
        Throwable error = excepcion;
        assertThrows(Pedido.EstadoInvalidoException.class, () -> {
            throw error;
        });
    }
}