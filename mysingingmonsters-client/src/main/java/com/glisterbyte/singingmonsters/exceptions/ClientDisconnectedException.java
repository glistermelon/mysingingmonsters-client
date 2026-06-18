package com.glisterbyte.singingmonsters.exceptions;

public class ClientDisconnectedException extends ClientException {

    public ClientDisconnectedException() {
        super("Client is disconnected");
    }

}