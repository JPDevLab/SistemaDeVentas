package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ApiError> handleStockInsuficiente(StockInsuficienteException ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(), "STOCK_INSUFICIENTE", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ProductoInactivoException.class)
    public ResponseEntity<ApiError> handleProductoInactivo(ProductoInactivoException ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(), "PRODUCTO_INACTIVO", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PedidoSinProductosException.class)
    public ResponseEntity<ApiError> handlePedidoSinProductos(PedidoSinProductosException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "PEDIDO_SIN_PRODUCTOS", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PedidoNoCancelableException.class)
    public ResponseEntity<ApiError> handlePedidoNoCancelable(PedidoNoCancelableException ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(), "PEDIDO_NO_CANCELABLE", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PrecioInvalidoException.class)
    public ResponseEntity<ApiError> handlePrecioInvalido(PrecioInvalidoException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "PRECIO_INVALIDO", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CantidadInvalidaException.class)
    public ResponseEntity<ApiError> handleCantidadInvalida(CantidadInvalidaException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "CANTIDAD_INVALIDA", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ApiError> handleProductoNoEncontrado(ProductoNoEncontradoException ex) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND.value(), "PRODUCTO_NO_ENCONTRADO", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PedidoNoEncontradoException.class)
    public ResponseEntity<ApiError> handlePedidoNoEncontrado(PedidoNoEncontradoException ex) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND.value(), "PEDIDO_NO_ENCONTRADO", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "VALIDACION_INVALIDA", message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.example.model.Pedido.EstadoInvalidoException.class)
    public ResponseEntity<ApiError> handleEstadoInvalido(org.example.model.Pedido.EstadoInvalidoException ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(), "ESTADO_INVALIDO", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(org.example.model.Pedido.PedidoSinProductosException.class)
    public ResponseEntity<ApiError> handlePedidoSinProductosDominio(org.example.model.Pedido.PedidoSinProductosException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "PEDIDO_SIN_PRODUCTOS", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.example.model.Producto.StockInsuficienteException.class)
    public ResponseEntity<ApiError> handleStockInsuficienteDominio(org.example.model.Producto.StockInsuficienteException ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(), "STOCK_INSUFICIENTE", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(org.example.model.Producto.ProductoInactivoException.class)
    public ResponseEntity<ApiError> handleProductoInactivoDominio(org.example.model.Producto.ProductoInactivoException ex) {
        ApiError error = new ApiError(HttpStatus.CONFLICT.value(), "PRODUCTO_INACTIVO", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(org.example.model.Producto.PrecioInvalidoException.class)
    public ResponseEntity<ApiError> handlePrecioInvalidoDominio(org.example.model.Producto.PrecioInvalidoException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "PRECIO_INVALIDO", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            org.example.model.Producto.CantidadInvalidaException.class,
            org.example.model.MovimientoInventario.CantidadInvalidaException.class
    })
    public ResponseEntity<ApiError> handleCantidadInvalidaDominio(RuntimeException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), "CANTIDAD_INVALIDA", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
