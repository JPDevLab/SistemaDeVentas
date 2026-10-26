import { api, mensajeError } from "../api.js";
import { formatCurrency, formatDate } from "../format.js";
import { h, clear, emptyState, estadoBadge, showToast } from "../ui.js";

const ESTADOS = ["CREADO", "PAGADO", "ENTREGADO", "CANCELADO"];
const STOCK_BAJO = 5;

export async function render(container) {
    clear(container);
    container.append(
        h(
            "div",
            { class: "page-head" },
            h(
                "div",
                {},
                h("h1", { class: "page-title" }, "Dashboard"),
                h("p", { class: "page-subtitle" }, "Resumen general de productos, inventario y pedidos."),
            ),
            h(
                "div",
                { class: "row" },
                h("a", { class: "button button--ghost", href: "#/inventario" }, "Inventario"),
                h("a", { class: "button button--primary", href: "#/pedidos" }, "Nuevo pedido"),
            ),
        ),
    );

    const contenido = h("div", { class: "stack" });
    container.append(contenido);

    try {
        const [productos, pedidos, movimientos] = await Promise.all([
            api.listarProductos(),
            api.listarPedidos(),
            api.listarMovimientos(),
        ]);
        renderResumen(contenido, productos, pedidos, movimientos);
    } catch (error) {
        showToast(mensajeError(error), "error");
        contenido.append(h(
            "section",
            { class: "card" },
            h("div", { class: "card__body" }, emptyState(
                "No se pudo cargar el resumen",
                mensajeError(error),
            )),
        ));
    }
}

function renderResumen(contenido, productos, pedidos, movimientos) {
    const activos = productos.filter((p) => p.estado === "ACTIVO");
    const stockBajo = productos
        .filter((p) => Number(p.stock) <= STOCK_BAJO)
        .sort((a, b) => Number(a.stock) - Number(b.stock));
    const validos = pedidos.filter((p) => p.estado !== "CANCELADO");
    const ventas = validos.reduce((total, pedido) => total + Number(pedido.totalFinal ?? 0), 0);
    const unidadesVendidas = validos.reduce((total, pedido) => {
        return total + (pedido.detalles ?? []).reduce((suma, detalle) => suma + Number(detalle.cantidad ?? 0), 0);
    }, 0);
    const ultimasEntradas = movimientos
        .filter((movimiento) => movimiento.tipo === "ENTRADA")
        .slice(0, 1)
        .length;

    contenido.append(
        h(
            "div",
            { class: "stats-grid" },
            stat("Productos", productos.length, `${activos.length} activos`),
            stat("Pedidos", pedidos.length, `${validos.length} vigentes`),
            stat("Ventas", formatCurrency(ventas), `${unidadesVendidas} unidades vendidas`),
            stat("Stock bajo", stockBajo.length, `≤ ${STOCK_BAJO} unidades`),
        ),
        h(
            "div",
            { class: "grid-2" },
            h(
                "section",
                { class: "card" },
                h("div", { class: "card__header" }, h("h2", { class: "card__title" }, "Pedidos por estado")),
                h(
                    "div",
                    { class: "card__body" },
                    h(
                        "div",
                        { class: "state-list" },
                        ...ESTADOS.map((estadoNombre) => h(
                            "span",
                            { class: "state-item" },
                            estadoBadge(estadoNombre),
                            h("strong", {}, pedidos.filter((p) => p.estado === estadoNombre).length),
                        )),
                        h(
                            "span",
                            { class: "state-item" },
                            h("span", { class: "muted" }, "Movimientos"),
                            h("strong", {}, movimientos.length),
                        ),
                    ),
                    h(
                        "p",
                        { class: "summary__hint" },
                        ultimasEntradas
                            ? "El historial de movimientos refleja entradas manuales y salidas por pedidos."
                            : "Aún no se han registrado entradas manuales de inventario.",
                    ),
                ),
            ),
            h(
                "section",
                { class: "card" },
                h(
                    "div",
                    { class: "card__header" },
                    h("h2", { class: "card__title" }, "Alerta de stock"),
                    h("a", { class: "button button--ghost button--sm", href: "#/inventario" }, "Reabastecer"),
                ),
                h("div", { class: "card__body" }, stockBajo.length === 0
                    ? emptyState("Stock saludable", `Ningún producto tiene ${STOCK_BAJO} unidades o menos.`)
                    : h("div", { class: "table-wrap" }, h(
                        "table",
                        { class: "table" },
                        h(
                            "thead",
                            {},
                            h("tr", {}, h("th", {}, "Producto"), h("th", { class: "table__num" }, "Stock")),
                        ),
                        h(
                            "tbody",
                            {},
                            ...stockBajo.slice(0, 5).map((producto) => h(
                                "tr",
                                {},
                                h("td", {}, producto.nombre),
                                h("td", { class: "table__num table__num--low" }, producto.stock),
                            )),
                        ),
                    ))),
            ),
        ),
        h(
            "section",
            { class: "card" },
            h(
                "div",
                { class: "card__header" },
                h("h2", { class: "card__title" }, "Últimos pedidos"),
                h("a", { class: "button button--ghost button--sm", href: "#/pedidos" }, "Ver todos"),
            ),
            h("div", { class: "card__body" }, pedidos.length === 0
                ? emptyState("Todavía no hay pedidos", "Crea un pedido para verlo reflejado aquí.")
                : h("div", { class: "table-wrap" }, h(
                    "table",
                    { class: "table" },
                    h(
                        "thead",
                        {},
                        h(
                            "tr",
                            {},
                            h("th", {}, "Pedido"),
                            h("th", {}, "Fecha"),
                            h("th", {}, "Cliente"),
                            h("th", { class: "table__num" }, "Total final"),
                            h("th", {}, "Estado"),
                        ),
                    ),
                    h(
                        "tbody",
                        {},
                        ...[...pedidos]
                            .sort((a, b) => b.id - a.id)
                            .slice(0, 5)
                            .map((pedido) => h(
                                "tr",
                                {},
                                h("td", {}, h("strong", {}, `#${pedido.id}`)),
                                h("td", { class: "muted" }, formatDate(pedido.fecha)),
                                h("td", {}, pedido.cliente),
                                h("td", { class: "table__num" }, formatCurrency(pedido.totalFinal)),
                                h("td", {}, estadoBadge(pedido.estado)),
                            )),
                    ),
                ))),
        ),
    );
}

function stat(label, value, hint) {
    return h(
        "div",
        { class: "stat" },
        h("div", { class: "stat__label" }, label),
        h("p", { class: "stat__value" }, value),
        h("p", { class: "stat__hint" }, hint),
    );
}
