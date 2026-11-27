package com.learning.taskmanager.event;

import com.learning.taskmanager.model.Task;

import java.util.*;

public class AuditTrailListener implements TaskEventListener {
    private final Map<String, Integer> counts = new HashMap<>();

    public void onEvent(String e, Task t) {
        counts.merge(e, 1, Integer::sum);
    }

    public Map<String, Integer> getCounts() {
        return Collections.unmodifiableMap(counts);
    }
}
