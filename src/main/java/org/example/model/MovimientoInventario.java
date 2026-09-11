package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "movimientos_inventario")
public class MovimientoInventario {

    public enum TipoMovimiento {
        ENTRADA,
        SALIDA
    }

    public static class CantidadInvalidaException extends RuntimeException {
        public CantidadInvalidaException(String message) {
            super(message);
        }
    }

    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Bogota");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    protected MovimientoInventario() {
    }

    public MovimientoInventario(Producto producto, Integer cantidad, TipoMovimiento tipo) {
        this.producto = producto;
        setCantidad(cantidad);
        this.tipo = tipo;
        this.fecha = LocalDateTime.now(ZONA_HORARIA);
    }

    public static MovimientoInventario entrada(Producto producto, int cantidad) {
        return new MovimientoInventario(producto, cantidad, TipoMovimiento.ENTRADA);
    }

    public static MovimientoInventario salida(Producto producto, int cantidad) {
        return new MovimientoInventario(producto, cantidad, TipoMovimiento.SALIDA);
    }

    public void setCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new CantidadInvalidaException("La cantidad debe ser mayor que cero");
        }
        this.cantidad = cantidad;
    }

    public Long getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}