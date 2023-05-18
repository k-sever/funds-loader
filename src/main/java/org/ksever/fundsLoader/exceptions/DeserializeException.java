package org.ksever.fundsLoader.exceptions;

public class DeserializeException extends RuntimeException {

    public DeserializeException(String message) {
        super(message);
    }

    public DeserializeException(String message, Exception e) {
        super(message, e);
    }
}
