package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InventarioPage extends BasePage {

    private static final By PRODUCTO = By.cssSelector("[data-testid='inventario-producto']");
    private static final By CANTIDAD = By.cssSelector("[data-testid='inventario-cantidad']");
    private static final By REGISTRAR = By.cssSelector("[data-testid='inventario-registrar']");
    private static final By STOCK = By.cssSelector("[data-testid='inventario-stock']");

    public void abrir() {
        openPath("/#/inventario");
        waitVisible(PRODUCTO);
        esperarOpciones();
    }

    public void seleccionarProducto(String nombre) {
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
        esperarStockNumerico();
    }

    public void registrarEntrada(int cantidad) {
        type(CANTIDAD, String.valueOf(cantidad));
        click(REGISTRAR);
    }

    public String stockActual() {
        esperarStockNumerico();
        return waitVisible(STOCK).getText().trim();
    }

    public void esperarStock(int esperado) {
        new WebDriverWait(getDriver(), TIMEOUT)
                .until(d -> waitVisible(STOCK).getText().trim().equals(String.valueOf(esperado)));
    }

    public boolean existeMovimientoEntrada(String nombre, int cantidad) {
        By movimiento = By.xpath("//tr[@data-testid='movimiento-row'][@data-producto="
                + xpathText(nombre) + "][@data-tipo='ENTRADA'][@data-cantidad='" + cantidad + "']");
        return waitVisible(movimiento).isDisplayed();
    }

    private void esperarOpciones() {
        waitVisible(PRODUCTO);
        new WebDriverWait(getDriver(), TIMEOUT).until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector("[data-testid='inventario-producto'] option"), 1));
    }

    private void esperarStockNumerico() {
        new WebDriverWait(getDriver(), TIMEOUT)
                .until(d -> waitVisible(STOCK).getText().trim().matches("\\d+"));
    }
}
