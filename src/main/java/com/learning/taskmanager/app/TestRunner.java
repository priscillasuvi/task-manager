package com.learning.taskmanager.app;

import com.learning.taskmanager.api.*;
import com.learning.taskmanager.model.*;
import com.learning.taskmanager.repository.*;
import com.learning.taskmanager.parser.*;
import com.learning.taskmanager.service.*;

public class TestRunner {
    private static int pass = 0, fail = 0;

    private static void check(String n, boolean ok) {
        if (ok) {
            pass++;
            System.out.println("PASS " + n);
        } else {
            fail++;
            System.out.println("FAIL " + n);
        }
    }

    public static void main(String[] args) {
        InMemoryTaskRepository repo = InMemoryTaskRepository.getInstance();
        repo.clear();
        IdempotencyStore.getInstance().clear();
        FakeLlmClient fake = new FakeLlmClient("{\"title\":\"Call bank\",\"description\":\"Discuss account\",\"dueDate\":null,\"recurrence\":null}");
        TaskService s = new TaskService(repo, new LlmTaskParser(fake), new RuleBasedBreakdownStrategy());
        TaskApi api = new TaskApi(s);
        TaskRequest valid = new TaskRequest("Task", "Description", "false", null, "MEDIUM");
        check("create", api.createTask(valid, "same").getStatusCode() == 201);
        check("idempotent retry", api.createTask(valid, "same").getStatusCode() == 201 && repo.findAll().size() == 1);
        check("key conflict", api.createTask(new TaskRequest("Other", "Desc", "false", null, "LOW"), "same").getStatusCode() == 422);
        check("validation", api.createTask(new TaskRequest(" ", "", "x", "2026-02-30", "bad"), null).getStatusCode() == 400);
        check("not found", api.getTask(999).getStatusCode() == 404);
        check("AI create", api.createTaskFromText("Call bank", "ai-key").getStatusCode() == 201 && fake.getCalls() == 1);
        check("AI retry no second call", api.createTaskFromText("Call bank", "ai-key").getStatusCode() == 201 && fake.getCalls() == 1);
        Task sub = s.subtask(1, new TaskRequest("Child", "Child details", "false", null, "LOW"));
        check("subtask of subtask rejected", api.createSubtask(sub.getId(), valid).getStatusCode() == 400);
        check("pagination beyond end", api.listTasks(TaskQuery.builder().page(100).size(2).build()).getStatusCode() == 200);
        check("bad AI output returns 502", DemoFactory.create(new LlmTaskParser(new FakeLlmClient("{\"title\":\"\",\"description\":\"x\",\"dueDate\":null,\"recurrence\":null}")), new RuleBasedBreakdownStrategy()).createTaskFromText("x", null).getStatusCode() == 502);
        System.out.println("Tests: " + pass + " passed, " + fail + " failed");
    }
}
