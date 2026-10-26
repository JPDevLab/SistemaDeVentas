import { api, mensajeError } from "../api.js";
import { calcularTotales, formatCurrency, formatDate, formatPercent } from "../format.js";
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

const ACCIONABLES = new Set(["CREADO", "PAGADO"]);

export async function render(container) {
    clear(container);

    const estado = {
        productos: [],
        pedidos: [],
        carrito: new Map(),
        busqueda: "",
    };

    const productSelect = h("select", { class: "input", "aria-label": "Producto a agregar" });
    const cantidad = h("input", {
        class: "input input--sm",
        type: "number",
        min: "1",
        step: "1",
        value: "1",
        "aria-label": "Cantidad",
    });
    const agregar = h("button", { class: "button button--ghost", type: "button" }, "Agregar");
    const cartBody = h("tbody");
    const cliente = h("input", {
        class: "input",
        type: "text",
        maxlength: "150",
        placeholder: "Nombre del cliente",
    });
    const totalValor = h("span", {}, formatCurrency(0));
    const descuentoValor = h("span", {}, formatCurrency(0));
    const finalValor = h("span", {}, formatCurrency(0));
    const descuentoRow = h(
        "div",
        { class: "summary__row summary__row--discount" },
        h("span", {}, "Descuento"),
        descuentoValor,
    );
    const formError = h("p", { class: "form-error", role: "alert" });
    const crear = h("button", { class: "button button--primary", type: "button", disabled: true }, "Crear pedido");

    const buscador = h("input", {
        class: "input",
        type: "search",
        placeholder: "Buscar por cliente o número de pedido…",
        "aria-label": "Buscar pedidos",
        on: {
            input: (event) => {
                estado.busqueda = event.target.value.trim().toLowerCase();
                renderPedidos();
            },
        },
    });
    const pedidosBody = h("tbody");

    const orderSide = h(
        "aside",
        { class: "order-side" },
        h(
            "div",
            { class: "stack" },
            h(
                "label",
                { class: "form-field" },
                h("span", { class: "form-label" }, "Cliente"),
                cliente,
            ),
            h(
                "div",
                { class: "summary" },
                h("div", { class: "summary__row" }, h("span", {}, "Subtotal"), totalValor),
                descuentoRow,
                h("div", { class: "summary__row summary__row--total" }, h("span", {}, "Total"), finalValor),
            ),
            h("p", { class: "summary__hint" },
                "Descuentos automáticos: 5 % desde $100.000, 10 % desde $300.000 y 15 % desde $500.000."),
            formError,
            crear,
        ),
    );

    const builder = h(
        "section",
        { class: "card" },
        h("div", { class: "card__header" }, h("h2", { class: "card__title" }, "Nuevo pedido")),
        h(
            "div",
            { class: "card__body" },
            h(
                "div",
                { class: "order-builder" },
                h(
                    "div",
                    { class: "stack" },
                    h("div", { class: "toolbar" }, productSelect, cantidad, agregar),
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
                                h("th", { class: "table__num" }, "Precio"),
                                h("th", { class: "table__num" }, "Cantidad"),
                                h("th", { class: "table__num" }, "Subtotal"),
                                h("th", {}, ""),
                            ),
                        ),
                        cartBody,
                    )),
                ),
                orderSide,
            ),
        ),
    );

    const pedidosCard = h(
        "section",
        { class: "card" },
        h(
            "div",
            { class: "card__header" },
            h("h2", { class: "card__title" }, "Pedidos"),
            h("button", {
                class: "button button--ghost button--sm",
                type: "button",
                on: { click: () => cargarPedidos() },
            }, "Recargar"),
        ),
        h(
            "div",
            { class: "card__body" },
            h("div", { class: "toolbar" }, buscador),
            h("div", { class: "table-wrap" }, h(
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
                        h("th", {}, "Acciones"),
                    ),
                ),
                pedidosBody,
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
                h("h1", { class: "page-title" }, "Pedidos"),
                h("p", { class: "page-subtitle" }, "Crea pedidos con descuentos automáticos y gestiona su estado."),
            ),
        ),
        builder,
        pedidosCard,
    );

    function productosDisponibles() {
        return estado.productos
            .filter((producto) => producto.estado === "ACTIVO")
            .sort((a, b) => a.nombre.localeCompare(b.nombre, "es"));
    }

    function llenarSelectProductos() {
        const previo = productSelect.value;
        clear(productSelect);
        productSelect.append(h("option", { value: "" }, "Selecciona un producto…"));
        for (const producto of productosDisponibles()) {
            productSelect.append(h(
                "option",
                { value: String(producto.id) },
                `${producto.nombre} · ${formatCurrency(producto.precio)} · stock ${producto.stock}`,
            ));
        }
        if (previo && productosDisponibles().some((p) => String(p.id) === previo)) {
            productSelect.value = previo;
        }
    }

    function renderCarrito() {
        clear(cartBody);
        const items = [...estado.carrito.values()];

        if (items.length === 0) {
            cartBody.append(h(
                "tr",
                {},
                h("td", { colspan: "5" }, emptyState(
                    "El pedido está vacío",
                    "Agrega productos con el selector de arriba.",
                )),
            ));
        } else {
            for (const item of items) {
                const producto = item.producto;
                const cantidadInput = h("input", {
                    class: "input input--sm",
                    type: "number",
                    min: "1",
                    step: "1",
                    value: String(item.cantidad),
                    "aria-label": `Cantidad de ${producto.nombre}`,
                });
                cantidadInput.addEventListener("change", () => {
                    const valor = Number(cantidadInput.value);
                    if (!Number.isInteger(valor) || valor <= 0) {
                        showToast("La cantidad debe ser un entero mayor que cero.", "error");
                        cantidadInput.value = String(item.cantidad);
                        return;
                    }
                    if (valor > Number(producto.stock)) {
                        showToast(`Stock insuficiente: solo hay ${producto.stock} unidades.`, "error");
                        cantidadInput.value = String(item.cantidad);
                        return;
                    }
                    item.cantidad = valor;
                    renderCarrito();
                    actualizarResumen();
                });

                cartBody.append(h(
                    "tr",
                    {},
                    h("td", {}, h("strong", {}, producto.nombre)),
                    h("td", { class: "table__num" }, formatCurrency(producto.precio)),
                    h("td", { class: "table__num" }, cantidadInput),
                    h("td", { class: "table__num" }, formatCurrency(Number(producto.precio) * item.cantidad)),
                    h("td", {}, h("button", {
                        class: "icon-button",
                        type: "button",
                        "aria-label": `Quitar ${producto.nombre}`,
                        on: {
                            click: () => {
                                estado.carrito.delete(producto.id);
                                renderCarrito();
                                actualizarResumen();
                            },
                        },
                    }, "×")),
                ));
            }
        }
        actualizarResumen();
    }

    function actualizarResumen() {
        const items = [...estado.carrito.values()].map((item) => ({
            precio: item.producto.precio,
            cantidad: item.cantidad,
        }));
        const totales = calcularTotales(items);
        totalValor.textContent = formatCurrency(totales.total);
        descuentoValor.textContent = totales.porcentaje > 0
            ? `- ${formatCurrency(totales.descuento)} (${formatPercent(totales.porcentaje)})`
            : formatCurrency(0);
        descuentoRow.style.display = totales.porcentaje > 0 ? "" : "none";
        finalValor.textContent = formatCurrency(totales.totalFinal);
        crear.disabled = estado.carrito.size === 0;
    }

    agregar.addEventListener("click", () => {
        formError.textContent = "";
        const producto = estado.productos.find((p) => String(p.id) === productSelect.value);
        if (!producto) {
            showToast("Selecciona un producto.", "error");
            return;
        }
        const valor = Number(cantidad.value);
        if (!Number.isInteger(valor) || valor <= 0) {
            showToast("Ingresa una cantidad entera mayor que cero.", "error");
            return;
        }
        const actual = estado.carrito.get(producto.id)?.cantidad ?? 0;
        if (actual + valor > Number(producto.stock)) {
            showToast(`Stock insuficiente: solo hay ${producto.stock} unidades de ${producto.nombre}.`, "error");
            return;
        }
        estado.carrito.set(producto.id, { producto, cantidad: actual + valor });
        cantidad.value = "1";
        renderCarrito();
    });

    crear.addEventListener("click", async () => {
        formError.textContent = "";
        const nombreCliente = cliente.value.trim();
        if (!nombreCliente) {
            formError.textContent = "Ingresa el nombre del cliente.";
            cliente.focus();
            return;
        }
        if (estado.carrito.size === 0) {
            formError.textContent = "Agrega al menos un producto.";
            return;
        }

        crear.disabled = true;
        try {
            await api.crearPedido({
                cliente: nombreCliente,
                detalles: [...estado.carrito.values()].map((item) => ({
                    productoId: item.producto.id,
                    cantidad: item.cantidad,
                })),
            });
            estado.carrito.clear();
            cliente.value = "";
            renderCarrito();
            showToast("Pedido creado.", "success");
            await Promise.all([cargarProductos(), cargarPedidos()]);
        } catch (error) {
            formError.textContent = mensajeError(error);
        } finally {
            crear.disabled = estado.carrito.size === 0;
        }
    });

    function renderPedidos() {
        const term = estado.busqueda;
        const visibles = estado.pedidos.filter((pedido) => {
            if (!term) {
                return true;
            }
            return String(pedido.cliente ?? "").toLowerCase().includes(term)
                || String(pedido.id).includes(term);
        });

        clear(pedidosBody);
        if (visibles.length === 0) {
            pedidosBody.append(h(
                "tr",
                {},
                h("td", { colspan: "6" }, emptyState(
                    estado.pedidos.length === 0 ? "Aún no hay pedidos" : "Sin resultados",
                    estado.pedidos.length === 0
                        ? "Crea el primer pedido desde el formulario de arriba."
                        : "Ajusta la búsqueda por cliente o número.",
                )),
            ));
            return;
        }

        for (const pedido of [...visibles].sort((a, b) => b.id - a.id)) {
            const acciones = h("div", { class: "actions" });
            acciones.append(h("button", {
                class: "button button--ghost button--sm",
                type: "button",
                on: { click: () => verDetalle(pedido.id) },
            }, "Detalle"));

            if (ACCIONABLES.has(pedido.estado)) {
                acciones.append(h("button", {
                    class: "button button--ghost button--sm",
                    type: "button",
                    on: { click: () => entregar(pedido) },
                }, pedido.estado === "CREADO" ? "Pagar y entregar" : "Entregar"));
                acciones.append(h("button", {
                    class: "button button--danger button--sm",
                    type: "button",
                    on: { click: () => cancelar(pedido) },
                }, "Cancelar"));
            }

            pedidosBody.append(h(
                "tr",
                {},
                h("td", {}, h("strong", {}, `#${pedido.id}`)),
                h("td", { class: "muted" }, formatDate(pedido.fecha)),
                h("td", {}, pedido.cliente),
                h("td", { class: "table__num" }, formatCurrency(pedido.totalFinal)),
                h("td", {}, estadoBadge(pedido.estado)),
                h("td", {}, acciones),
            ));
        }
    }

    async function verDetalle(id) {
        const modal = createModal(`Pedido #${id}`);
        modal.body.append(h("div", { class: "loading" }, h("span", { class: "spinner", "aria-hidden": "true" }), "Cargando…"));
        try {
            const pedido = await api.obtenerPedido(id);
            clear(modal.body);
            modal.body.append(
                h(
                    "div",
                    { class: "detail-grid" },
                    h("div", { class: "detail-item" }, h("div", { class: "detail-label" }, "Cliente"), h("div", { class: "detail-value" }, pedido.cliente)),
                    h("div", { class: "detail-item" }, h("div", { class: "detail-label" }, "Fecha"), h("div", { class: "detail-value" }, formatDate(pedido.fecha))),
                    h("div", { class: "detail-item" }, h("div", { class: "detail-label" }, "Estado"), h("div", { class: "detail-value" }, estadoBadge(pedido.estado))),
                    h("div", { class: "detail-item" }, h("div", { class: "detail-label" }, "Subtotal"), h("div", { class: "detail-value" }, formatCurrency(pedido.total))),
                    h("div", { class: "detail-item" }, h("div", { class: "detail-label" }, "Descuento"), h("div", { class: "detail-value" }, formatCurrency(pedido.descuento))),
                    h("div", { class: "detail-item" }, h("div", { class: "detail-label" }, "Total final"), h("div", { class: "detail-value" }, formatCurrency(pedido.totalFinal))),
                ),
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
                            h("th", { class: "table__num" }, "Cantidad"),
                            h("th", { class: "table__num" }, "Precio unitario"),
                            h("th", { class: "table__num" }, "Subtotal"),
                        ),
                    ),
                    h(
                        "tbody",
                        {},
                        ...(pedido.detalles ?? []).map((detalle) => h(
                            "tr",
                            {},
                            h("td", {}, detalle.productoNombre),
                            h("td", { class: "table__num" }, detalle.cantidad),
                            h("td", { class: "table__num" }, formatCurrency(detalle.precioUnitario)),
                            h("td", { class: "table__num" }, formatCurrency(detalle.subtotal)),
                        )),
                    ),
                )),
            );
            modal.footer.append(h("button", { class: "button button--ghost", type: "button", on: { click: modal.close } }, "Cerrar"));
        } catch (error) {
            clear(modal.body);
            modal.body.append(emptyState("No se pudo cargar el pedido", mensajeError(error)));
        }
    }

    async function entregar(pedido) {
        const confirmado = await confirmDialog(
            `¿Marcar el pedido #${pedido.id} como entregado? Esta acción descuenta el stock definitivamente.`,
            { title: "Entregar pedido", confirmLabel: "Entregar" },
        );
        if (!confirmado) {
            return;
        }
        try {
            await api.entregarPedido(pedido.id);
            showToast(`Pedido #${pedido.id} entregado.`, "success");
            await Promise.all([cargarPedidos(), cargarProductos()]);
        } catch (error) {
            showToast(mensajeError(error), "error");
        }
    }

    async function cancelar(pedido) {
        const confirmado = await confirmDialog(
            `¿Cancelar el pedido #${pedido.id}? El stock reservado será restaurado.`,
            { title: "Cancelar pedido", confirmLabel: "Cancelar pedido", danger: true },
        );
        if (!confirmado) {
            return;
        }
        try {
            await api.cancelarPedido(pedido.id);
            showToast(`Pedido #${pedido.id} cancelado.`, "success");
            await Promise.all([cargarPedidos(), cargarProductos()]);
        } catch (error) {
            showToast(mensajeError(error), "error");
        }
    }

    async function cargarProductos() {
        try {
            estado.productos = await api.listarProductos();
            llenarSelectProductos();
            for (const item of estado.carrito.values()) {
                const actualizado = estado.productos.find((p) => p.id === item.producto.id);
                if (actualizado) {
                    item.producto = actualizado;
                }
            }
            renderCarrito();
        } catch (error) {
            showToast(mensajeError(error), "error");
        }
    }

    async function cargarPedidos() {
        tableLoading(pedidosBody, 6);
        try {
            estado.pedidos = await api.listarPedidos();
            renderPedidos();
        } catch (error) {
            clear(pedidosBody);
            pedidosBody.append(h(
                "tr",
                {},
                h("td", { colspan: "6" }, emptyState("No se pudieron cargar los pedidos", mensajeError(error))),
            ));
        }
    }

    renderCarrito();
    await cargarProductos();
    await cargarPedidos();
}
