export function h(tag, props = {}, ...children) {
    const element = document.createElement(tag);

    for (const [key, value] of Object.entries(props ?? {})) {
        if (value === null || value === undefined || value === false) {
            continue;
        }
        switch (key) {
            case "class":
                element.className = value;
                break;
            case "text":
                element.textContent = value;
                break;
            case "html":
                element.innerHTML = value;
                break;
            case "on":
                for (const [event, handler] of Object.entries(value)) {
                    element.addEventListener(event, handler);
                }
                break;
            case "dataset":
                Object.assign(element.dataset, value);
                break;
            case "value":
                element.value = value;
                break;
            case "checked":
            case "disabled":
            case "selected":
                element[key] = Boolean(value);
                break;
            default:
                if (key === "for") {
                    element.htmlFor = value;
                } else {
                    element.setAttribute(key, value);
                }
        }
    }

    for (const child of children.flat(Infinity)) {
        if (child === null || child === undefined || child === false) {
            continue;
        }
        element.append(child instanceof Node ? child : document.createTextNode(String(child)));
    }

    return element;
}

export function clear(element) {
    while (element.firstChild) {
        element.removeChild(element.firstChild);
    }
}

const ESTADOS = {
    ACTIVO: ["success", "Activo"],
    INACTIVO: ["neutral", "Inactivo"],
    CREADO: ["info", "Creado"],
    PAGADO: ["warning", "Pagado"],
    ENTREGADO: ["success", "Entregado"],
    CANCELADO: ["danger", "Cancelado"],
    ENTRADA: ["success", "Entrada"],
    SALIDA: ["warning", "Salida"],
};

export function estadoBadge(estado) {
    const [variant, label] = ESTADOS[estado] ?? ["neutral", estado ?? "—"];
    return h("span", { class: `badge badge--${variant}` }, label);
}

export function emptyState(message, hint) {
    return h(
        "div",
        { class: "empty" },
        h("p", { class: "empty__title" }, message),
        hint ? h("p", { class: "empty__hint" }, hint) : null,
    );
}

export function loading(container, message = "Cargando…") {
    clear(container);
    container.append(
        h("div", { class: "loading" }, h("span", { class: "spinner", "aria-hidden": "true" }), message),
    );
}

export function tableLoading(tbody, colspan = 6, message = "Cargando…") {
    clear(tbody);
    tbody.append(h(
        "tr",
        {},
        h(
            "td",
            { colspan: String(colspan) },
            h("div", { class: "loading" }, h("span", { class: "spinner", "aria-hidden": "true" }), message),
        ),
    ));
}

export function showToast(message, type = "info") {
    const container = document.getElementById("toast-container");
    if (!container) {
        return;
    }
    const toast = h("div", { class: `toast toast--${type}`, role: "status" }, message);
    container.append(toast);
    window.setTimeout(() => {
        toast.classList.add("toast--out");
        window.setTimeout(() => toast.remove(), 250);
    }, 4200);
}

export function createModal(title) {
    const dialog = h("dialog", { class: "modal" });

    function close() {
        if (dialog.open) {
            dialog.close();
        }
        dialog.remove();
    }

    const header = h(
        "div",
        { class: "modal__header" },
        h("h2", { class: "modal__title" }, title),
        h("button", {
            class: "icon-button",
            type: "button",
            "aria-label": "Cerrar",
            on: { click: close },
        }, "×"),
    );
    const body = h("div", { class: "modal__body" });
    const footer = h("div", { class: "modal__footer" });

    dialog.append(header, body, footer);
    dialog.addEventListener("cancel", (event) => {
        event.preventDefault();
        close();
    });
    dialog.addEventListener("close", () => dialog.remove());
    document.body.append(dialog);
    dialog.showModal();

    return { dialog, body, footer, close };
}

export function confirmDialog(message, options = {}) {
    const {
        title = "Confirmar",
        confirmLabel = "Aceptar",
        cancelLabel = "Cancelar",
        danger = false,
    } = options;

    return new Promise((resolve) => {
        const modal = createModal(title);
        let settled = false;

        function finish(value) {
            if (settled) {
                return;
            }
            settled = true;
            resolve(value);
            modal.close();
        }

        modal.body.append(h("p", { class: "confirm-text" }, message));
        modal.footer.append(
            h("button", {
                class: "button button--ghost",
                type: "button",
                on: { click: () => finish(false) },
            }, cancelLabel),
            h("button", {
                class: `button ${danger ? "button--danger" : "button--primary"}`,
                type: "button",
                on: { click: () => finish(true) },
            }, confirmLabel),
        );
        modal.dialog.addEventListener("close", () => finish(false));
    });
}
