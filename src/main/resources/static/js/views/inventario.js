import { api, mensajeError } from "../api.js";
import { formatDate } from "../format.js";
import {
    h,
    clear,
    showToast,
    emptyState,
    tableLoading,
    estadoBadge,
} from "../ui.js";

export async function render(container) {
    clear(container);

    const estado = {
        productos: [],
        movimientos: [],
        productoId: "",
        stock: null,
    };

    const productoSelect = h("select", {
        class: "input",
        "aria-label": "Producto",
        on: {
            change: async (event) => {
                estado.productoId = event.target.value;
                await actualizarStock();
                renderMovimientos();
            },
        },
    });

    const stockValor = h("p", { class: "stat__value" }, "—");
    const stockHint = h("p", { class: "stat__hint" }, "Selecciona un producto para consultar su stock.");

    const cantidad = h("input", {
        class: "input",
        type: "number",
        min: "1",
        step: "1",
        value: "1",
        "aria-label": "Cantidad de entrada",
    });

    const entradaError = h("p", { class: "form-error", role: "alert" });
    const registrar = h("button", { class: "button button--primary", type: "submit" }, "Registrar entrada");

    const movimientosBody = h("tbody");
    const busquedaProducto = h("input", {
        class: "input",
        type: "search",
        placeholder: "Filtrar movimientos por producto…",
        "aria-label": "Filtrar movimientos",
        on: {
            input: (event) => {
                estado.busqueda = event.target.value.trim().toLowerCase();
                renderMovimientos();
            },
        },
    });

    const formEntrada = h(
        "form",
        { class: "form" },
        h(
            "label",
            { class: "form-field" },
            h("span", { class: "form-label" }, "Producto"),
            productoSelect,
        ),
        h(
            "label",
            { class: "form-field" },
            h("span", { class: "form-label" }, "Cantidad a ingresar"),
            cantidad,
            h("span", { class: "form-hint" }, "Solo se permiten cantidades enteras mayores que cero."),
        ),
        entradaError,
        h("div", { class: "form-actions" }, registrar),
    );

    formEntrada.addEventListener("submit", async (event) => {
        event.preventDefault();
        entradaError.textContent = "";
        if (!estado.productoId) {
            entradaError.textContent = "Selecciona un producto.";
            return;
        }
        const valor = Number(cantidad.value);
        if (!Number.isInteger(valor) || valor <= 0) {
            entradaError.textContent = "Ingresa una cantidad entera mayor que cero.";
            return;
        }
        registrar.disabled = true;
        try {
            await api.registrarEntrada({ productoId: Number(estado.productoId), cantidad: valor });
            showToast(`Entrada de ${valor} unidad(es) registrada.`, "success");
            cantidad.value = "1";
            await cargarMovimientos();
            await actualizarStock();
        } catch (error) {
            entradaError.textContent = mensajeError(error);
        } finally {
            registrar.disabled = false;
        }
    });

    container.append(
        h(
            "div",
            { class: "page-head" },
            h(
                "div",
                {},
                h("h1", { class: "page-title" }, "Inventario"),
                h("p", { class: "page-subtitle" }, "Registra entradas y consulta el historial de movimientos."),
            ),
            h("button", {
                class: "button button--ghost",
                type: "button",
                on: { click: () => cargarMovimientos() },
            }, "Recargar"),
        ),
        h(
            "div",
            { class: "grid-2" },
            h(
                "section",
                { class: "card" },
                h("div", { class: "card__header" }, h("h2", { class: "card__title" }, "Registrar entrada")),
                h("div", { class: "card__body" }, formEntrada),
            ),
            h(
                "section",
                { class: "card" },
                h("div", { class: "card__header" }, h("h2", { class: "card__title" }, "Stock actual")),
                h(
                    "div",
                    { class: "card__body" },
                    h("p", { class: "stat__label" }, "Unidades disponibles"),
                    stockValor,
                    stockHint,
                ),
            ),
        ),
        h(
            "section",
            { class: "card" },
            h(
                "div",
                { class: "card__header" },
                h("h2", { class: "card__title" }, "Movimientos"),
                h("span", { class: "muted" }, "Entradas y salidas por pedido"),
            ),
            h(
                "div",
                { class: "card__body" },
                h("div", { class: "toolbar" }, busquedaProducto),
                h("div", { class: "table-wrap" }, h(
                    "table",
                    { class: "table" },
                    h(
                        "thead",
                        {},
                        h(
                            "tr",
                            {},
                            h("th", {}, "Fecha"),
                            h("th", {}, "Producto"),
                            h("th", {}, "Tipo"),
                            h("th", { class: "table__num" }, "Cantidad"),
                        ),
                    ),
                    movimientosBody,
                )),
            ),
        ),
    );

    function productosParaSelect() {
        return estado.productos
            .filter((producto) => producto.estado === "ACTIVO")
            .sort((a, b) => a.nombre.localeCompare(b.nombre, "es"));
    }

    function llenarSelects() {
        const valorPrevio = estado.productoId;
        clear(productoSelect);
        productoSelect.append(h("option", { value: "" }, "Selecciona un producto…"));
        for (const producto of productosParaSelect()) {
            productoSelect.append(h("option", { value: String(producto.id) }, producto.nombre));
        }
        if (valorPrevio && estado.productos.some((p) => String(p.id) === valorPrevio)) {
            productoSelect.value = valorPrevio;
        } else {
            estado.productoId = "";
        }
    }

    async function actualizarStock() {
        if (!estado.productoId) {
            stockValor.textContent = "—";
            stockHint.textContent = "Selecciona un producto para consultar su stock.";
            return;
        }
        const producto = estado.productos.find((p) => String(p.id) === estado.productoId);
        stockHint.textContent = producto ? producto.nombre : "Producto seleccionado";
        try {
            estado.stock = await api.consultarStock(estado.productoId);
            stockValor.textContent = estado.stock;
        } catch (error) {
            estado.stock = null;
            stockValor.textContent = "—";
            stockHint.textContent = mensajeError(error);
        }
    }

    function renderMovimientos() {
        const term = estado.busqueda ?? "";
        const visibles = estado.movimientos.filter((movimiento) => {
            const coincideProducto = !estado.productoId || String(movimiento.productoId) === estado.productoId;
            const coincideTexto = !term || String(movimiento.productoNombre ?? "").toLowerCase().includes(term);
            return coincideProducto && coincideTexto;
        });

        clear(movimientosBody);
        if (visibles.length === 0) {
            movimientosBody.append(h(
                "tr",
                {},
                h("td", { colspan: "4" }, emptyState(
                    estado.movimientos.length === 0 ? "Sin movimientos registrados" : "Sin resultados",
                    estado.movimientos.length === 0
                        ? "Registra una entrada para comenzar el historial."
                        : "Cambia el filtro de producto o la búsqueda.",
                )),
            ));
            return;
        }

        for (const movimiento of visibles) {
            movimientosBody.append(h(
                "tr",
                {},
                h("td", { class: "muted" }, formatDate(movimiento.fecha)),
                h("td", {}, movimiento.productoNombre ?? `Producto #${movimiento.productoId}`),
                h("td", {}, estadoBadge(movimiento.tipo)),
                h("td", { class: "table__num" }, movimiento.cantidad),
            ));
        }
    }

    async function cargarMovimientos() {
        tableLoading(movimientosBody, 4);
        try {
            estado.movimientos = await api.listarMovimientos();
            renderMovimientos();
        } catch (error) {
            clear(movimientosBody);
            movimientosBody.append(h(
                "tr",
                {},
                h("td", { colspan: "4" }, emptyState("No se pudieron cargar los movimientos", mensajeError(error))),
            ));
        }
    }

    async function cargar() {
        try {
            estado.productos = await api.listarProductos();
            llenarSelects();
            await actualizarStock();
        } catch (error) {
            showToast(mensajeError(error), "error");
        }
        await cargarMovimientos();
    }

    await cargar();
}
