package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PedidosPage extends BasePage {

    private static final By PRODUCTO = By.cssSelector("[data-testid='pedido-producto']");
    private static final By CANTIDAD = By.cssSelector("[data-testid='pedido-cantidad']");
    private static final By AGREGAR = By.cssSelector("[data-testid='pedido-agregar']");
    private static final By CLIENTE = By.cssSelector("[data-testid='pedido-cliente']");
    private static final By CREAR = By.cssSelector("[data-testid='pedido-crear']");
    private static final By MODAL = By.cssSelector("[data-testid='modal']");
    private static final By CONFIRMAR = By.cssSelector("[data-testid='confirmar-accion']");

    public void abrir() {
        openPath("/#/pedidos");
        waitVisible(PRODUCTO);
        esperarOpciones();
    }

    public void crearPedido(String cliente, String producto, int cantidad) {
        seleccionarProducto(producto);
        type(CANTIDAD, String.valueOf(cantidad));
        click(AGREGAR);
        waitClickable(CREAR);
        type(CLIENTE, cliente);
        click(CREAR);
        waitVisible(fila(cliente));
        esperarEstado(cliente, "Creado");
    }

    public void cancelarPedido(String cliente) {
        WebElement fila = waitVisible(fila(cliente));
        waitUntilNoToasts();
        fila.findElement(By.cssSelector("[data-testid='pedido-cancelar']")).click();
        waitVisible(MODAL);
        click(CONFIRMAR);
        esperarEstado(cliente, "Cancelado");
    }

    public String estadoDePedido(String cliente) {
        return waitVisible(fila(cliente))
                .findElement(By.cssSelector("[data-testid='pedido-estado']"))
                .getText()
                .trim();
    }

    public void esperarEstado(String cliente, String estado) {
        By estadoCell = By.xpath("//tr[@data-testid='pedido-row'][@data-cliente="
                + xpathText(cliente) + "]//*[@data-testid='pedido-estado']");
        waitUntilText(estadoCell, estado);
    }

    public String totalFinalDePedido(String cliente) {
        return waitVisible(fila(cliente))
                .findElement(By.cssSelector("[data-testid='pedido-total-final-fila']"))
                .getText()
                .trim();
    }

    public void esperarTotalFinal(String cliente, String total) {
        By totalCell = By.xpath("//tr[@data-testid='pedido-row'][@data-cliente="
                + xpathText(cliente) + "]//*[@data-testid='pedido-total-final-fila']");
        waitUntilContainsText(totalCell, total);
    }

    private void seleccionarProducto(String nombre) {
        WebElement selectElement = waitVisible(PRODUCTO);
        new WebDriverWait(getDriver(), TIMEOUT).until(d -> {
            Select select = new Select(selectElement);
            return select.getOptions().stream()
                    .anyMatch(option -> option.getText().contains(nombre));
        });
        Select select = new Select(selectElement);
        WebElement option = select.getOptions().stream()
                .filter(item -> item.getText().contains(nombre))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No se encontró el producto " + nombre));
        select.selectByValue(option.getAttribute("value"));
    }

    private void esperarOpciones() {
        new WebDriverWait(getDriver(), TIMEOUT).until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("[data-testid='pedido-producto'] option"), 1));
    }

    private By fila(String cliente) {
        return By.xpath("//tr[@data-testid='pedido-row'][@data-cliente=" + xpathText(cliente) + "]");
    }
}
