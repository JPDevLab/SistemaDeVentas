package definitions;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import net.thucydides.core.annotations.Steps;
import steps.LoginSteps;

public class LoginDefinitions {

    @Steps
    LoginSteps loginSteps;

    @Dado("que el usuario abre la página de inicio de sesión")
    public void abrirPaginaLogin() {
        loginSteps.abrirPaginaLogin();
    }

    @Dado("que el usuario inicia sesión con credenciales válidas")
    public void iniciarSesionValida() {
        loginSteps.iniciarSesionValida();
    }

    @Cuando("ingresa el usuario {string} y la contraseña {string}")
    public void ingresarCredenciales(String usuario, String contrasena) {
        loginSteps.ingresarCredenciales(usuario, contrasena);
    }

    @Entonces("debe ver el panel principal")
    public void verificarPanelPrincipal() {
        loginSteps.verificarPanelPrincipal();
    }

    @Y("debe ver el usuario autenticado en la barra superior")
    public void verificarUsuarioEnBarra() {
        loginSteps.verificarUsuarioEnBarra("admin");
    }

    @Entonces("debe ver el mensaje {string}")
    public void verificarMensajeError(String mensaje) {
        loginSteps.verificarMensajeError(mensaje);
    }

    @Y("debe permanecer en la página de inicio de sesión")
    public void verificarPermaneceEnLogin() {
        loginSteps.verificarPermaneceEnLogin();
    }
}
