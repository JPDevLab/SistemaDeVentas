package org.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {

    public enum EstadoProducto {
        ACTIVO,
        INACTIVO
    }

    public static class ProductoInactivoException extends RuntimeException {
        public ProductoInactivoException(String message) {
            super(message);
        }
    }

    public static class StockInsuficienteException extends RuntimeException {
        public StockInsuficienteException(String message) {
            super(message);
        }
    }

    public static class CantidadInvalidaException extends RuntimeException {
        public CantidadInvalidaException(String message) {
            super(message);
        }
    }

    public static class PrecioInvalidoException extends RuntimeException {
        public PrecioInvalidoException(String message) {
            super(message);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoProducto estado = EstadoProducto.ACTIVO;

    @Version
    private Long version;

    protected Producto() {
    }

    public Producto(String nombre, String descripcion, BigDecimal precio, int stock, EstadoProducto estado) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        setPrecio(precio);
        setStock(stock);
        this.estado = estado == null ? EstadoProducto.ACTIVO : estado;
    }

    public void descontarStock(int cantidad) {
        validarCantidad(cantidad);
        if (estado != EstadoProducto.ACTIVO) {
            throw new ProductoInactivoException("El producto " + nombre + " está inactivo");
        }
        long stockResultante = (long) stock - cantidad;
        if (stockResultante < 0) {
            throw new StockInsuficienteException("No existe inventario suficiente para completar la compra");
        }
        this.stock = (int) stockResultante;
    }

    public void reponerStock(int cantidad) {
        validarCantidad(cantidad);
        this.stock += cantidad;
    }

    public void setPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PrecioInvalidoException("El precio debe ser mayor que cero");
        }
        this.precio = precio;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new CantidadInvalidaException("El stock no puede ser negativo");
        }
        this.stock = stock;
    }

    private void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new CantidadInvalidaException("La cantidad debe ser mayor que cero");
        }
    }

    public boolean estaActivo() {
        return estado == EstadoProducto.ACTIVO;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Integer getStock() {
        return stock;
    }

    public EstadoProducto getEstado() {
        return estado;
    }

    public void setEstado(EstadoProducto estado) {
        this.estado = estado;
    }

    public Long getVersion() {
        return version;
    }
}