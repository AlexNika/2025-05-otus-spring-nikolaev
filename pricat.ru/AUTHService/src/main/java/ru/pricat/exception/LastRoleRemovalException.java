package ru.pricat.exception;

public class LastRoleRemovalException extends RuntimeException {
    public LastRoleRemovalException(String message) {
        super(message);
    }

    public LastRoleRemovalException(String message, Throwable cause) {
        super(message, cause);
    }
}