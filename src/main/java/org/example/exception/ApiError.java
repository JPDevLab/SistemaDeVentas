package org.example.exception;

public record ApiError(
        int status,
        String error,
        String message
) {
}
