package org.example.auth;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public static final String SESSION_USER = "usuarioAutenticado";

    private static final String AUTENTICADO_KEY = "autenticado";
    private static final String USUARIO_KEY = "usuario";

    private final boolean enabled;
    private final String usuario;
    private final String clave;

    public AuthController(@Value("${app.auth.enabled:true}") boolean enabled,
                          @Value("${app.auth.usuario:admin}") String usuario,
                          @Value("${app.auth.clave:admin123}") String clave) {
        this.enabled = enabled;
        this.usuario = usuario;
        this.clave = clave;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request, HttpSession session) {
        if (!enabled) {
            session.setAttribute(SESSION_USER, "demo");
            return ResponseEntity.ok(Map.of(AUTENTICADO_KEY, true, USUARIO_KEY, "demo"));
        }
        if (request == null || !coincide(request.usuario(), usuario) || !coincide(request.contrasena(), clave)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", "CREDENCIALES_INVALIDAS",
                            "message", "Usuario o contraseña incorrectos"
                    ));
        }
        session.setAttribute(SESSION_USER, usuario);
        return ResponseEntity.ok(Map.of(AUTENTICADO_KEY, true, USUARIO_KEY, usuario));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpSession session) {
        Object autenticado = session.getAttribute(SESSION_USER);
        if (autenticado == null && !enabled) {
            return ResponseEntity.ok(Map.of(
                    AUTENTICADO_KEY, true,
                    USUARIO_KEY, "demo",
                    "authDisabled", true
            ));
        }
        if (autenticado == null) {
            return ResponseEntity.ok(Map.of(AUTENTICADO_KEY, false));
        }
        return ResponseEntity.ok(Map.of(AUTENTICADO_KEY, true, USUARIO_KEY, autenticado));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    private boolean coincide(String valor, String esperado) {
        if (valor == null || esperado == null) {
            return false;
        }
        return MessageDigest.isEqual(
                valor.getBytes(StandardCharsets.UTF_8),
                esperado.getBytes(StandardCharsets.UTF_8)
        );
    }

    public record LoginRequest(String usuario, String contrasena) {
    }
}
