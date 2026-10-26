package org.example.exception;

public class PrecioInvalidoException extends RuntimeException {
    public PrecioInvalidoException(String message) {
        super(message);
    }
}
