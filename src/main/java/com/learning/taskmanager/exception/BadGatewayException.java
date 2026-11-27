package com.learning.taskmanager.exception;

public class BadGatewayException extends ApiException {
    public BadGatewayException(String m) {
        super(502, "BAD_GATEWAY", m);
    }

    public BadGatewayException(String m, Throwable t) {
        super(502, "BAD_GATEWAY", m);
        initCause(t);
    }
}
