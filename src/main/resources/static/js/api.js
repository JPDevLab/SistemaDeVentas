const JSON_HEADERS = { "Content-Type": "application/json" };

export class ApiError extends Error {
    constructor(status, code, message) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.code = code;
    }
}

async function request(path, options = {}) {
    const { body, headers, ...rest } = options;
    let response;
    try {
        response = await fetch(path, {
            ...rest,
            headers: {
                Accept: "application/json",
                ...(body ? JSON_HEADERS : {}),
                ...(headers || {}),
            },
            body,
        });
    } catch (error) {
        throw new ApiError(0, "SIN_CONEXION", "No se pudo conectar con el servidor.");
    }

    const text = await response.text();
    let data = null;
    if (text) {
        try {
            data = JSON.parse(text);
        } catch (error) {
            data = text;
        }
    }

    if (!response.ok) {
        const code = data && typeof data === "object" ? data.error : null;
        const message = data && typeof data === "object"
            ? data.message
            : (typeof data === "string" && data ? data : `Error HTTP ${response.status}`);
        throw new ApiError(response.status, code, message);
    }

    return data;
}

function jsonBody(value) {
    return JSON.stringify(value);
}

export const api = {
    listarProductos: () => request("/productos"),
    obtenerProducto: (id) => request(`/productos/${encodeURIComponent(id)}`),
    crearProducto: (producto) => request("/productos", { method: "POST", body: jsonBody(producto) }),
    actualizarProducto: (id, producto) => request(`/productos/${encodeURIComponent(id)}`, {
        method: "PUT",
        body: jsonBody(producto),
    }),

    registrarEntrada: (entrada) => request("/inventario/entradas", { method: "POST", body: jsonBody(entrada) }),
    consultarStock: (productoId) => request(`/inventario/${encodeURIComponent(productoId)}`),
    listarMovimientos: (productoId) => {
        const query = productoId ? `?productoId=${encodeURIComponent(productoId)}` : "";
        return request(`/inventario/movimientos${query}`);
    },

    listarPedidos: (cliente) => {
        const query = cliente ? `?cliente=${encodeURIComponent(cliente)}` : "";
        return request(`/pedidos${query}`);
    },
    obtenerPedido: (id) => request(`/pedidos/${encodeURIComponent(id)}`),
    crearPedido: (pedido) => request("/pedidos", { method: "POST", body: jsonBody(pedido) }),
    cancelarPedido: (id) => request(`/pedidos/${encodeURIComponent(id)}/cancelar`, { method: "PATCH" }),
    entregarPedido: (id) => request(`/pedidos/${encodeURIComponent(id)}/entregar`, { method: "PATCH" }),
};

export function mensajeError(error) {
    if (error instanceof ApiError) {
        return error.message || error.code || "Ocurrió un error.";
    }
    return "No se pudo conectar con el servidor.";
}
