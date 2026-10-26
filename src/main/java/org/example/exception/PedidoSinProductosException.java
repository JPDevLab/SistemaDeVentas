package org.example.exception;

public class PedidoSinProductosException extends RuntimeException {
    public PedidoSinProductosException(String message) {
        super(message);
    }
}
