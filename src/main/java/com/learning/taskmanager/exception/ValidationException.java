package com.learning.taskmanager.exception;

import java.util.*;

public class ValidationException extends ApiException {
    private final List<String[]> issues;

    public ValidationException(List<String[]> issues) {
        super(400, "VALIDATION_ERROR", "Validation failed");
        this.issues = issues;
    }

    public List<String[]> getIssues() {
        return issues;
    }
}
