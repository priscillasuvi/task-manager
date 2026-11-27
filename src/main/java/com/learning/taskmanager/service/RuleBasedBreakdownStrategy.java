package com.learning.taskmanager.service;

import com.learning.taskmanager.model.*;

import java.util.*;

public class RuleBasedBreakdownStrategy implements BreakdownStrategy {
    public List<TaskRequest> breakdown(Task t) {
        String[] parts = t.getTitle().split("(?i)\\s+and\\s+|,");
        List<TaskRequest> out = new ArrayList<>();
        for (String p : parts) {
            String s = p.trim();
            if (!s.isEmpty()) out.add(new TaskRequest(s, t.getDescription(), "false", null, "MEDIUM"));
        }
        return out;
    }
}
