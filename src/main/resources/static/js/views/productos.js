import { api, mensajeError } from "../api.js";
import { formatCurrency } from "../format.js";
import {
    h,
    clear,
    showToast,
    createModal,
    emptyState,
    tableLoading,
    estadoBadge,
    confirmDialog,
} from "../ui.js";

const ESTADOS = ["ACTIVO", "INACTIVO"];

export async function render(container) {
    clear(container);

    const estado = { productos: [], busqueda: "", filtro: "TODOS" };

    const search = h("input", {
        class: "input",
        type: "search",
        placeholder: "Buscar por nombre o descripción…",
        "aria-label": "Buscar productos",
        on: {
            input: (event) => {
                estado.busqueda = event.target.value.trim().toLowerCase();
                renderTabla();
            },
        },
    });

    const filter = h(
        "select",
        {
            class: "input",
            "aria-label": "Filtrar por estado",
            on: {
                change: (event) => {
                    estado.filtro = event.target.value;
                    renderTabla();
                },
            },
        },
        h("option", { value: "TODOS" }, "Todos los estados"),
        ...ESTADOS.map((value) => h("option", { value }, value === "ACTIVO" ? "Activos" : "Inactivos")),
    );

    const nuevo = h("button", {
        class: "button button--primary",
        type: "button",
        on: { click: () => abrirFormulario() },
    }, "Nuevo producto");

    const tableBody = h("tbody");

    const card = h(
        "section",
        { class: "card" },
        h(
            "div",
            { class: "card__header" },
            h("h2", { class: "card__title" }, "Catálogo"),
            h("button", {
                class: "button button--ghost button--sm",
                type: "button",
                on: { click: () => cargar() },
            }, "Recargar"),
        ),
        h(
            "div",
            { class: "card__body" },
            h("div", { class: "toolbar" }, search, filter),
            h("div", { class: "table-wrap" }, h(
                "table",
                { class: "table" },
                h(
                    "thead",
                    {},
                    h(
                        "tr",
                        {},
                        h("th", {}, "Producto"),
                        h("th", {}, "Descripción"),
                        h("th", { class: "table__num" }, "Precio"),
                        h("th", { class: "table__num" }, "Stock"),
                        h("th", {}, "Estado"),
                        h("th", {}, "Acciones"),
                    ),
                ),
                tableBody,
            )),
        ),
    );

    container.append(
        h(
            "div",
            { class: "page-head" },
            h(
                "div",
                {},
                h("h1", { class: "page-title" }, "Productos"),
                h("p", { class: "page-subtitle" }, "Administra el catálogo, precios y disponibilidad."),
            ),
            nuevo,
        ),
        card,
    );

    function renderTabla() {
        const term = estado.busqueda;
        const visibles = estado.productos.filter((producto) => {
            const coincideEstado = estado.filtro === "TODOS" || producto.estado === estado.filtro;
            const coincideTexto = !term
                || String(producto.nombre ?? "").toLowerCase().includes(term)
                || String(producto.descripcion ?? "").toLowerCase().includes(term);
            return coincideEstado && coincideTexto;
        });

        clear(tableBody);

        if (visibles.length === 0) {
            tableBody.append(h(
                "tr",
                {},
                h("td", { colspan: "6" }, emptyState(
                    estado.productos.length === 0 ? "Aún no hay productos" : "Sin resultados",
                    estado.productos.length === 0
                        ? "Crea el primer producto para empezar."
                        : "Ajusta la búsqueda o el filtro de estado.",
                )),
            ));
            return;
        }

        for (const producto of visibles) {
            const bajo = Number(producto.stock) <= 5;
            tableBody.append(h(
                "tr",
                {},
                h("td", {}, h("strong", {}, producto.nombre)),
                h("td", { class: "muted" }, producto.descripcion || "—"),
                h("td", { class: "table__num" }, formatCurrency(producto.precio)),
                h("td", { class: `table__num${bajo ? " table__num--low" : ""}` }, producto.stock),
                h("td", {}, estadoBadge(producto.estado)),
                h(
                    "td",
                    {},
                    h(
                        "div",
                        { class: "actions" },
                        h("button", {
                            class: "button button--ghost button--sm",
                            type: "button",
                            on: { click: () => abrirFormulario(producto) },
                        }, "Editar"),
                        h("button", {
                            class: "button button--ghost button--sm",
                            type: "button",
                            on: { click: () => cambiarEstado(producto) },
                        }, producto.estado === "ACTIVO" ? "Desactivar" : "Activar"),
                    ),
                ),
            ));
        }
    }

    async function cargar() {
        tableLoading(tableBody, 6);
        try {
            estado.productos = await api.listarProductos();
            renderTabla();
        } catch (error) {
            clear(tableBody);
            tableBody.append(h(
                "tr",
                {},
                h("td", { colspan: "6" }, emptyState("No se pudieron cargar los productos", mensajeError(error))),
            ));
        }
    }

    function abrirFormulario(producto) {
        const editando = Boolean(producto);
        const modal = createModal(editando ? "Editar producto" : "Nuevo producto");
        const error = h("p", { class: "form-error", role: "alert" });
        const guardar = h("button", { class: "button button--primary", type: "submit" }, editando ? "Guardar cambios" : "Crear producto");

        const nombre = h("input", {
            class: "input",
            name: "nombre",
            required: true,
            maxlength: "120",
            value: producto?.nombre ?? "",
        });
        const descripcion = h("textarea", {
            class: "input",
            name: "descripcion",
            rows: "3",
            maxlength: "500",
            value: producto?.descripcion ?? "",
        });
        const precio = h("input", {
            class: "input",
            name: "precio",
            type: "number",
            min: "0",
            step: "0.01",
            required: true,
            value: producto?.precio ?? "",
        });
        const stock = h("input", {
            class: "input",
            name: "stock",
            type: "number",
            min: "0",
            step: "1",
            required: !editando,
            value: producto?.stock ?? 0,
            disabled: editando,
        });
        const estadoSelect = h(
            "select",
            { class: "input", name: "estado" },
            ...ESTADOS.map((value) => h("option", { value }, value)),
        );
        estadoSelect.value = producto?.estado ?? "ACTIVO";

        const form = h(
            "form",
            { class: "form", novalidate: true },
            h(
                "div",
                { class: "form-grid" },
                h("label", { class: "form-field" }, h("span", { class: "form-label" }, "Nombre"), nombre),
                h("label", { class: "form-field" }, h("span", { class: "form-label" }, "Precio (COP)"), precio),
                h(
                    "label",
                    { class: "form-field" },
                    h("span", { class: "form-label" }, "Stock"),
                    stock,
                    h("span", { class: "form-hint" }, editando
                        ? "El stock se ajusta desde Inventario."
                        : "Stock inicial disponible."),
                ),
                h("label", { class: "form-field" }, h("span", { class: "form-label" }, "Estado"), estadoSelect),
            ),
            h("label", { class: "form-field" }, h("span", { class: "form-label" }, "Descripción"), descripcion),
            error,
            h("div", { class: "form-actions" }, h("button", {
                class: "button button--ghost",
                type: "button",
                on: { click: modal.close },
            }, "Cancelar"), guardar),
        );

        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            error.textContent = "";
            guardar.disabled = true;

            const payload = {
                nombre: nombre.value.trim(),
                descripcion: descripcion.value.trim(),
                precio: Number(precio.value),
                stock: editando ? Number(producto.stock) : Number(stock.value),
                estado: estadoSelect.value,
            };

            if (!payload.nombre) {
                error.textContent = "El nombre es obligatorio.";
                guardar.disabled = false;
                return;
            }
            if (!Number.isFinite(payload.precio) || payload.precio < 0) {
                error.textContent = "Ingresa un precio válido.";
                guardar.disabled = false;
                return;
            }
            if (!Number.isInteger(payload.stock) || payload.stock < 0) {
                error.textContent = "Ingresa un stock válido (entero mayor o igual a 0).";
                guardar.disabled = false;
                return;
            }

            try {
                if (editando) {
                    await api.actualizarProducto(producto.id, payload);
                } else {
                    await api.crearProducto(payload);
                }
                modal.close();
                showToast(editando ? "Producto actualizado." : "Producto creado.", "success");
                await cargar();
            } catch (err) {
                error.textContent = mensajeError(err);
                guardar.disabled = false;
            }
        });

        modal.body.append(form);
        nombre.focus();
    }

    async function cambiarEstado(producto) {
        const activar = producto.estado !== "ACTIVO";
        const confirmado = await confirmDialog(
            activar
                ? `¿Activar el producto "${producto.nombre}"? Volverá a estar disponible para pedidos.`
                : `¿Desactivar el producto "${producto.nombre}"? Dejará de estar disponible para nuevos pedidos.`,
            {
                title: activar ? "Activar producto" : "Desactivar producto",
                confirmLabel: activar ? "Activar" : "Desactivar",
                danger: !activar,
            },
        );
        if (!confirmado) {
            return;
        }
        try {
            await api.actualizarProducto(producto.id, {
                nombre: producto.nombre,
                descripcion: producto.descripcion,
                precio: producto.precio,
                stock: producto.stock,
                estado: activar ? "ACTIVO" : "INACTIVO",
            });
            showToast(activar ? "Producto activado." : "Producto desactivado.", "success");
            await cargar();
        } catch (error) {
            showToast(mensajeError(error), "error");
        }
    }

    await cargar();
}
