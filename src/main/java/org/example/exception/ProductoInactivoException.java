package org.example.exception;

public class ProductoInactivoException extends RuntimeException {
    public ProductoInactivoException(String message) {
        super(message);
    }
}
