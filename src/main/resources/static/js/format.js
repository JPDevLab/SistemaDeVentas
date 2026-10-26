const currencyFormatter = new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
});

const percentFormatter = new Intl.NumberFormat("es-CO", {
    style: "percent",
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
});

export function formatCurrency(value) {
    const number = Number(value ?? 0);
    return currencyFormatter.format(Number.isFinite(number) ? number : 0);
}

export function formatPercent(value) {
    return percentFormatter.format(Number(value ?? 0));
}

export function formatDate(value) {
    if (!value) {
        return "—";
    }
    const text = String(value);
    const match = text.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/);
    if (match) {
        return `${match[3]}/${match[2]}/${match[1]} ${match[4]}:${match[5]}`;
    }
    return text;
}

export function aCentavos(value) {
    return Math.round(Number(value ?? 0) * 100);
}

export function calcularTotales(items) {
    const totalCents = items.reduce((acumulado, item) => {
        return acumulado + aCentavos(item.precio) * Number(item.cantidad ?? 0);
    }, 0);

    let porcentaje = 0;
    if (totalCents >= 50000000) {
        porcentaje = 0.15;
    } else if (totalCents >= 30000000) {
        porcentaje = 0.10;
    } else if (totalCents >= 10000000) {
        porcentaje = 0.05;
    }

    const descuentoCents = Math.round(totalCents * porcentaje);
    return {
        total: totalCents / 100,
        descuento: descuentoCents / 100,
        totalFinal: (totalCents - descuentoCents) / 100,
        porcentaje,
    };
}
