package org.example.exception;

public class CantidadInvalidaException extends RuntimeException {
    public CantidadInvalidaException(String message) {
        super(message);
    }
}
