package org.example.exception;

public class PedidoNoCancelableException extends RuntimeException {
    public PedidoNoCancelableException(String message) {
        super(message);
    }
}
