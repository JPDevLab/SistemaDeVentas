package pages;

import org.openqa.selenium.By;

public class DashboardPage extends BasePage {

    private static final By TITULO = By.cssSelector("h1.page-title");
    private static final By USUARIO = By.cssSelector("[data-testid='current-user']");

    public void esperarPanelPrincipal() {
        waitVisible(TITULO);
        waitUntilText(TITULO, "Dashboard");
    }

    public String titulo() {
        return waitVisible(TITULO).getText().trim();
    }

    public String usuarioActual() {
        return waitVisible(USUARIO).getText().trim();
    }
}
