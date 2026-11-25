package org.example.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthTest {

    private static final String USUARIO = "admin";
    private static final String CLAVE = "admin123";
    private static final String AUTENTICADO_KEY = "autenticado";
    private static final String USUARIO_KEY = "usuario";
    private static final String ERROR_KEY = "error";
    private static final String DEMO = "demo";
    private static final String CREDENCIALES_INVALIDAS = "CREDENCIALES_INVALIDAS";
    private static final String NO_AUTENTICADO = "NO_AUTENTICADO";

    private static final AuthController.LoginRequest LOGIN_VALIDO = new AuthController.LoginRequest(USUARIO, CLAVE);

    private MockHttpSession sesion;
    private AuthController controlador;
    private AuthInterceptor interceptor;

    @BeforeEach
    void configurar() {
        sesion = new MockHttpSession();
        controlador = new AuthController(true, USUARIO, CLAVE);
        interceptor = new AuthInterceptor(true);
    }

    @Test
    void loginValidoCreaSesion() {
        ResponseEntity<Map<String, Object>> respuesta = controlador.login(LOGIN_VALIDO, sesion);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(Boolean.TRUE, respuesta.getBody().get(AUTENTICADO_KEY));
        assertEquals(USUARIO, respuesta.getBody().get(USUARIO_KEY));
        assertEquals(USUARIO, sesion.getAttribute(AuthController.SESSION_USER));
    }

    @Test
    void loginConCredencialesInvalidasDevuelveNoAutorizado() {
        ResponseEntity<Map<String, Object>> respuesta = controlador.login(
                new AuthController.LoginRequest(USUARIO, "claveIncorrecta"), sesion);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertEquals(CREDENCIALES_INVALIDAS, respuesta.getBody().get(ERROR_KEY));
        assertNull(sesion.getAttribute(AuthController.SESSION_USER));
    }

    @Test
    void loginConUsuarioNuloDevuelveNoAutorizado() {
        ResponseEntity<Map<String, Object>> respuesta = controlador.login(
                new AuthController.LoginRequest(null, CLAVE), sesion);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertEquals(CREDENCIALES_INVALIDAS, respuesta.getBody().get(ERROR_KEY));
    }

    @Test
    void loginConClaveNulaDevuelveNoAutorizado() {
        ResponseEntity<Map<String, Object>> respuesta = controlador.login(
                new AuthController.LoginRequest(USUARIO, null), sesion);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertEquals(CREDENCIALES_INVALIDAS, respuesta.getBody().get(ERROR_KEY));
    }

    @Test
    void loginSinCuerpoDevuelveNoAutorizado() {
        ResponseEntity<Map<String, Object>> respuesta = controlador.login(null, sesion);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertEquals(CREDENCIALES_INVALIDAS, respuesta.getBody().get(ERROR_KEY));
    }

    @Test
    void loginConCredencialesEsperadasNulasDevuelveNoAutorizado() {
        AuthController sinClave = new AuthController(true, USUARIO, null);

        ResponseEntity<Map<String, Object>> respuesta = sinClave.login(LOGIN_VALIDO, sesion);

        assertEquals(HttpStatus.UNAUTHORIZED, respuesta.getStatusCode());
        assertEquals(CREDENCIALES_INVALIDAS, respuesta.getBody().get(ERROR_KEY));
    }

    @Test
    void loginDesactivadoAceptaCualquierSolicitud() {
        AuthController desactivado = new AuthController(false, USUARIO, CLAVE);

        ResponseEntity<Map<String, Object>> respuesta = desactivado.login(
                new AuthController.LoginRequest("otro", "otra"), sesion);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(DEMO, respuesta.getBody().get(USUARIO_KEY));
        assertEquals(DEMO, sesion.getAttribute(AuthController.SESSION_USER));
    }

    @Test
    void meSinSesionDevuelveNoAutenticado() {
        ResponseEntity<Map<String, Object>> respuesta = controlador.me(sesion);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(Boolean.FALSE, respuesta.getBody().get(AUTENTICADO_KEY));
    }

    @Test
    void meConSesionAutenticadaDevuelveUsuario() {
        sesion.setAttribute(AuthController.SESSION_USER, USUARIO);

        ResponseEntity<Map<String, Object>> respuesta = controlador.me(sesion);

        assertEquals(Boolean.TRUE, respuesta.getBody().get(AUTENTICADO_KEY));
        assertEquals(USUARIO, respuesta.getBody().get(USUARIO_KEY));
    }

    @Test
    void meConAutenticacionDesactivadaDevuelveUsuarioDemo() {
        AuthController desactivado = new AuthController(false, USUARIO, CLAVE);

        ResponseEntity<Map<String, Object>> respuesta = desactivado.me(sesion);

        assertEquals(Boolean.TRUE, respuesta.getBody().get(AUTENTICADO_KEY));
        assertEquals(DEMO, respuesta.getBody().get(USUARIO_KEY));
        assertEquals(Boolean.TRUE, respuesta.getBody().get("authDisabled"));
    }

    @Test
    void logoutInvalidaLaSesion() {
        sesion.setAttribute(AuthController.SESSION_USER, USUARIO);

        ResponseEntity<Void> respuesta = controlador.logout(sesion);

        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
        assertTrue(sesion.isInvalid());
        assertThrows(IllegalStateException.class,
                () -> sesion.getAttribute(AuthController.SESSION_USER));
    }

    @Test
    void interceptorPermiteSesionAutenticada() throws Exception {
        MockHttpServletRequest solicitud = new MockHttpServletRequest();
        solicitud.setSession(sesion);
        sesion.setAttribute(AuthController.SESSION_USER, USUARIO);
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(solicitud, respuesta, new Object()));
        assertEquals(200, respuesta.getStatus());
    }

    @Test
    void interceptorRechazaSolicitudSinSesion() throws Exception {
        MockHttpServletRequest solicitud = new MockHttpServletRequest();
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(solicitud, respuesta, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), respuesta.getStatus());
        assertTrue(respuesta.getContentAsString().contains(NO_AUTENTICADO));
    }

    @Test
    void interceptorRechazaSesionSinUsuarioAutenticado() throws Exception {
        MockHttpServletRequest solicitud = new MockHttpServletRequest();
        solicitud.setSession(sesion);
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(solicitud, respuesta, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), respuesta.getStatus());
        assertTrue(respuesta.getContentAsString().contains(NO_AUTENTICADO));
    }

    @Test
    void interceptorPermiteTodoCuandoLaAutenticacionEstaDesactivada() throws Exception {
        AuthInterceptor desactivado = new AuthInterceptor(false);
        MockHttpServletRequest solicitud = new MockHttpServletRequest();
        MockHttpServletResponse respuesta = new MockHttpServletResponse();

        assertTrue(desactivado.preHandle(solicitud, respuesta, new Object()));
        assertEquals(200, respuesta.getStatus());
    }
}
