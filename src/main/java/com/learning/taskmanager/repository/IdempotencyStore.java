package com.learning.taskmanager.repository;

import com.learning.taskmanager.model.ApiResponse;

import java.util.*;

public final class IdempotencyStore {
    private static final IdempotencyStore INSTANCE = new IdempotencyStore();
    private final Map<String, Record> records = new HashMap<>();

    private IdempotencyStore() {
    }

    public static IdempotencyStore getInstance() {
        return INSTANCE;
    }

    public synchronized ApiResponse get(String key, String fingerprint) {
        if (key == null || key.isBlank()) return null;
        Record r = records.get(key);
        if (r == null) return null;
        if (!r.fingerprint.equals(fingerprint))
            throw new com.learning.taskmanager.exception.ConflictException("Idempotency key reused with different request");
        return r.response;
    }

    public synchronized void put(String key, String fingerprint, ApiResponse response) {
        if (key != null && !key.isBlank()) records.put(key, new Record(fingerprint, response));
    }

    public synchronized void clear() {
        records.clear();
    }

    private static class Record {
        String fingerprint;
        ApiResponse response;

        Record(String f, ApiResponse r) {
            fingerprint = f;
            response = r;
        }
    }
}
