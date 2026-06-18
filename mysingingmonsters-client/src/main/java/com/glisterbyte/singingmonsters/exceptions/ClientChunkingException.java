package com.glisterbyte.singingmonsters.exceptions;

public class ClientChunkingException extends ClientException {

    public ClientChunkingException(String message) {
        super(message);
    }

    public ClientChunkingException(Throwable ex) {
        super(ex);
    }

}