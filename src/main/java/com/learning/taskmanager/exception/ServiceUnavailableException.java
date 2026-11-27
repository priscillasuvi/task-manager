package com.learning.taskmanager.exception;

public class ServiceUnavailableException extends ApiException {
    public ServiceUnavailableException(String m) {
        super(503, "SERVICE_UNAVAILABLE", m);
    }
}
