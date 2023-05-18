package org.ksever.fundsLoader.exceptions;

public class SerializeException extends RuntimeException {

    public SerializeException(String message, Exception e) {
        super(message, e);
    }
}
