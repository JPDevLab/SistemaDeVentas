package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ProductosPage extends BasePage {

    private static final By BUSCAR = By.cssSelector("[data-testid='producto-buscar']");
    private static final By NUEVO = By.cssSelector("[data-testid='producto-nuevo']");
    private static final By FORM = By.cssSelector("[data-testid='producto-form']");
    private static final By NOMBRE = By.cssSelector("[data-testid='producto-nombre']");
    private static final By DESCRIPCION = By.cssSelector("[data-testid='producto-descripcion']");
    private static final By PRECIO = By.cssSelector("[data-testid='producto-precio']");
    private static final By STOCK = By.cssSelector("[data-testid='producto-stock-input']");
    private static final By GUARDAR = By.cssSelector("[data-testid='producto-guardar']");

    public void abrir() {
        openPath("/#/productos");
        waitVisible(BUSCAR);
    }

    public void crearProducto(String nombre, String descripcion, String precio, int stock) {
        click(NUEVO);
        waitVisible(FORM);
        type(NOMBRE, nombre);
        type(DESCRIPCION, descripcion);
        type(PRECIO, precio);
        type(STOCK, String.valueOf(stock));
        click(GUARDAR);
        waitVisible(fila(nombre));
        buscarProducto(nombre);
    }

    public void buscarProducto(String nombre) {
        type(BUSCAR, nombre);
        waitVisible(fila(nombre));
    }

    public String stockDeProducto(String nombre) {
        buscarProducto(nombre);
        WebElement fila = waitVisible(fila(nombre));
        return fila.findElement(By.cssSelector("[data-testid='producto-stock']")).getText().trim();
    }

    public void esperarStock(String nombre, int stock) {
        By stockCell = By.xpath("//tr[@data-testid='producto-row'][@data-nombre="
                + xpathText(nombre) + "]//*[@data-testid='producto-stock']");
        waitUntilText(stockCell, String.valueOf(stock));
    }

    private By fila(String nombre) {
        return By.xpath("//tr[@data-testid='producto-row'][@data-nombre=" + xpathText(nombre) + "]");
    }
}
