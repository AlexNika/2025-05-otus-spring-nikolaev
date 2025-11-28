package ru.pricat.exception;

public class ClientServiceCommunicationException extends RuntimeException {
    public ClientServiceCommunicationException(String message) {
        super(message);
    }

    public ClientServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
