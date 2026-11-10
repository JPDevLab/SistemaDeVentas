package pages;

import org.openqa.selenium.By;

public class LoginPage extends BasePage {

    private static final By USUARIO = By.cssSelector("[data-testid='login-usuario']");
    private static final By CONTRASENA = By.cssSelector("[data-testid='login-contrasena']");
    private static final By BOTON = By.cssSelector("[data-testid='login-submit']");
    private static final By ERROR = By.cssSelector("[data-testid='login-error']");

    public void abrir() {
        getDriver().manage().deleteAllCookies();
        openPath("/login.html");
        waitVisible(USUARIO);
    }

    public void ingresar(String usuario, String contrasena) {
        type(USUARIO, usuario);
        type(CONTRASENA, contrasena);
        click(BOTON);
    }

    public String mensajeError() {
        waitVisible(ERROR);
        waitUntilContainsText(ERROR, "incorrectos");
        return waitVisible(ERROR).getText().trim();
    }

    public boolean estaEnLogin() {
        return getDriver().getCurrentUrl().contains("/login.html");
    }

    public boolean formularioVisible() {
        return waitVisible(BOTON).isDisplayed();
    }
}
