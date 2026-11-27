package com.learning.taskmanager.exception;

public class ConflictException extends ApiException {
    public ConflictException(String m) {
        super(422, "IDEMPOTENCY_CONFLICT", m);
    }
}
