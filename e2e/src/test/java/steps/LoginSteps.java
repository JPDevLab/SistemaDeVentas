package steps;

import pages.DashboardPage;
import pages.LoginPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginSteps {

    LoginPage loginPage;
    DashboardPage dashboardPage;

    public void abrirPaginaLogin() {
        loginPage.abrir();
    }

    public void ingresarCredenciales(String usuario, String contrasena) {
        loginPage.ingresar(usuario, contrasena);
    }

    public void iniciarSesionValida() {
        loginPage.abrir();
        loginPage.ingresar("admin", "admin123");
        dashboardPage.esperarPanelPrincipal();
    }

    public void verificarPanelPrincipal() {
        dashboardPage.esperarPanelPrincipal();
        assertEquals("Dashboard", dashboardPage.titulo());
    }

    public void verificarUsuarioEnBarra(String usuario) {
        assertTrue(dashboardPage.usuarioActual().contains(usuario));
    }

    public void verificarMensajeError(String mensaje) {
        assertTrue(loginPage.mensajeError().contains(mensaje));
    }

    public void verificarPermaneceEnLogin() {
        assertTrue(loginPage.estaEnLogin());
    }
}
