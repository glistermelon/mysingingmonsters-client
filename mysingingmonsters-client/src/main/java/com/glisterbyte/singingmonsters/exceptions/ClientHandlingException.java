package com.glisterbyte.singingmonsters.exceptions;

public class ClientHandlingException extends ClientException {

    public ClientHandlingException(String message, Throwable ex) {
        super(message, ex);
    }

    public ClientHandlingException(String message) {
        super(message);
    }

    public ClientHandlingException(Throwable ex) {
        super(ex);
    }

}