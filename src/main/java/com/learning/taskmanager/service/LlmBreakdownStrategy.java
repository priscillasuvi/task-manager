package com.learning.taskmanager.service;

import com.learning.taskmanager.model.*;
import com.learning.taskmanager.parser.LlmClient;
import com.learning.taskmanager.exception.*;

import java.util.*;

public class LlmBreakdownStrategy implements BreakdownStrategy {
    private final LlmClient client;

    public LlmBreakdownStrategy(LlmClient c) {
        client = c;
    }

    public List<TaskRequest> breakdown(Task t) {
        try {
            String p = "Break this task into 2-5 actionable subtasks. Return one per line as title|description only. Task: " + t.getTitle() + " — " + t.getDescription();
            List<TaskRequest> r = new ArrayList<>();
            for (String line : client.complete(p).split("\\R")) {
                String[] a = line.trim().split("\\|", 2);
                if (a.length == 2) r.add(new TaskRequest(a[0].trim(), a[1].trim(), "false", null, "MEDIUM"));
            }
            if (r.isEmpty()) throw new IllegalArgumentException("No subtasks");
            return r;
        } catch (ServiceUnavailableException e) {
            throw e;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new BadGatewayException("LLM breakdown failed", e);
        }
    }
}
