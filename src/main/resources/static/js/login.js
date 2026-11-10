const form = document.getElementById("login-form");
const error = document.getElementById("login-error");
const usuario = document.getElementById("login-usuario");
const contrasena = document.getElementById("login-contrasena");
const boton = form.querySelector("button[type='submit']");

async function comprobarSesion() {
    try {
        const response = await fetch("/auth/me", { headers: { Accept: "application/json" } });
        if (!response.ok) {
            return;
        }
        const data = await response.json();
        if (data.autenticado) {
            window.location.replace("/");
        }
    } catch (e) {
        return;
    }
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    error.textContent = "";
    boton.disabled = true;
    try {
        const response = await fetch("/auth/login", {
            method: "POST",
            headers: {
                Accept: "application/json",
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                usuario: usuario.value.trim(),
                contrasena: contrasena.value,
            }),
        });
        const data = await response.json().catch(() => null);
        if (!response.ok) {
            error.textContent = data?.message || "Usuario o contraseña incorrectos";
            return;
        }
        window.location.replace("/");
    } catch (e) {
        error.textContent = "No se pudo conectar con el servidor.";
    } finally {
        boton.disabled = false;
    }
});

usuario.focus();
comprobarSesion();
