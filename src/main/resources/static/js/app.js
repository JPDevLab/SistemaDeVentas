import { api, mensajeError } from "./api.js";
import { clear, emptyState, showToast } from "./ui.js";
import * as dashboard from "./views/dashboard.js";
import * as productos from "./views/productos.js";
import * as inventario from "./views/inventario.js";
import * as pedidos from "./views/pedidos.js";

const ROUTES = { dashboard, productos, inventario, pedidos };
const DEFAULT_ROUTE = "dashboard";
const app = document.getElementById("app");
const status = document.getElementById("api-status");

function rutaActual() {
    const hash = window.location.hash.replace(/^#\/?/, "");
    const nombre = hash.split("?")[0].trim();
    return Object.hasOwn(ROUTES, nombre) ? nombre : DEFAULT_ROUTE;
}

function marcarNavegacion(nombre) {
    for (const link of document.querySelectorAll(".app-nav a[data-route]")) {
        link.classList.toggle("is-active", link.dataset.route === nombre);
        if (link.dataset.route === nombre) {
            link.setAttribute("aria-current", "page");
        } else {
            link.removeAttribute("aria-current");
        }
    }
}

async function navegar() {
    const nombre = rutaActual();
    if (window.location.hash !== `#/${nombre}`) {
        window.history.replaceState(null, "", `#/${nombre}`);
    }
    marcarNavegacion(nombre);
    clear(app);
    try {
        await ROUTES[nombre].render(app);
    } catch (error) {
        clear(app);
        app.append(
            emptyState("No se pudo cargar la página", mensajeError(error)),
        );
        showToast(mensajeError(error), "error");
    }
    window.scrollTo({ top: 0, behavior: "smooth" });
}

async function verificarApi() {
    status.className = "status status--idle";
    status.textContent = "API: verificando…";
    try {
        await api.listarProductos();
        status.className = "status status--ok";
        status.textContent = "API: en línea";
    } catch (error) {
        status.className = "status status--error";
        status.textContent = "API: sin conexión";
        showToast(mensajeError(error), "error");
    }
}

status.addEventListener("click", verificarApi);
status.style.cursor = "pointer";
status.title = "Volver a verificar la conexión";

window.addEventListener("hashchange", navegar);
window.addEventListener("unhandledrejection", (event) => {
    showToast(mensajeError(event.reason), "error");
    event.preventDefault();
});

verificarApi();
navegar();
