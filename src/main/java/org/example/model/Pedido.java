package org.example.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    public enum EstadoPedido {
        CREADO,
        PAGADO,
        CANCELADO,
        ENTREGADO
    }

    public static class PedidoSinProductosException extends RuntimeException {
        public PedidoSinProductosException(String message) {
            super(message);
        }
    }

    public static class EstadoInvalidoException extends RuntimeException {
        public EstadoInvalidoException(String message) {
            super(message);
        }
    }

    @Embeddable
    public static class Detalle {

        @Column(name = "producto_id", nullable = false)
        private Long productoId;

        @Column(name = "nombre_producto", nullable = false, length = 100)
        private String nombreProducto;

        @Column(name = "cantidad", nullable = false)
        private int cantidad;

        @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
        private BigDecimal precioUnitario;

        @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
        private BigDecimal subtotal;

        protected Detalle() {
        }

        public Detalle(Long productoId, String nombreProducto, int cantidad, BigDecimal precioUnitario) {
            this.productoId = productoId;
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        }

        public Long getProductoId() {
            return productoId;
        }

        public String getNombreProducto() {
            return nombreProducto;
        }

        public int getCantidad() {
            return cantidad;
        }

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }
    }

    private static final BigDecimal DESCUENTO_5 = new BigDecimal("0.05");
    private static final BigDecimal DESCUENTO_10 = new BigDecimal("0.10");
    private static final BigDecimal DESCUENTO_15 = new BigDecimal("0.15");
    private static final BigDecimal UMBRAL_100_000 = new BigDecimal("100000");
    private static final BigDecimal UMBRAL_300_000 = new BigDecimal("300000");
    private static final BigDecimal UMBRAL_500_000 = new BigDecimal("500000");
    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Bogota");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 100)
    private String cliente;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalFinal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoPedido estado = EstadoPedido.CREADO;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "detalle_pedido", joinColumns = @JoinColumn(name = "pedido_id"))
    private List<Detalle> detalles = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(String cliente, LocalDateTime fecha) {
        this.cliente = cliente;
        this.fecha = fecha == null ? LocalDateTime.now(ZONA_HORARIA) : fecha;
    }

    public void agregarDetalle(Long productoId, String nombreProducto, int cantidad, BigDecimal precioUnitario) {
        detalles.add(new Detalle(productoId, nombreProducto, cantidad, precioUnitario));
        recalcularTotales();
    }

    public BigDecimal calcularPorcentajeDescuento() {
        BigDecimal base = total;
        if (base.compareTo(UMBRAL_500_000) >= 0) {
            return DESCUENTO_15;
        }
        if (base.compareTo(UMBRAL_300_000) >= 0) {
            return DESCUENTO_10;
        }
        if (base.compareTo(UMBRAL_100_000) >= 0) {
            return DESCUENTO_5;
        }
        return BigDecimal.ZERO;
    }

    public void recalcularTotales() {
        if (detalles.isEmpty()) {
            throw new PedidoSinProductosException("Un pedido debe contener al menos un producto");
        }
        BigDecimal sumaSubtotales = detalles.stream()
                .map(Detalle::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.total = sumaSubtotales;
        BigDecimal porcentaje = calcularPorcentajeDescuento();
        this.descuento = sumaSubtotales.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);
        this.totalFinal = sumaSubtotales.subtract(this.descuento);
    }

    public void cancelar() {
        if (estado == EstadoPedido.ENTREGADO) {
            throw new EstadoInvalidoException("Un pedido entregado no puede cancelarse");
        }
        if (estado == EstadoPedido.CANCELADO) {
            throw new EstadoInvalidoException("El pedido ya se encuentra cancelado");
        }
        this.estado = EstadoPedido.CANCELADO;
    }

    public void marcarPagado() {
        if (estado == EstadoPedido.CANCELADO || estado == EstadoPedido.ENTREGADO) {
            throw new EstadoInvalidoException("El pedido está en estado " + estado);
        }
        this.estado = EstadoPedido.PAGADO;
    }

    public void marcarEntregado() {
        if (estado != EstadoPedido.PAGADO) {
            throw new EstadoInvalidoException("Solo un pedido PAGADO puede marcarse como ENTREGADO");
        }
        this.estado = EstadoPedido.ENTREGADO;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public BigDecimal getTotalFinal() {
        return totalFinal;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public List<Detalle> getDetalles() {
        return detalles;
    }
}