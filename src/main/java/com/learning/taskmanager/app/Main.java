package com.learning.taskmanager.app;

import com.learning.taskmanager.api.TaskApi;
import com.learning.taskmanager.model.*;
import com.learning.taskmanager.repository.*;
import com.learning.taskmanager.service.*;
import com.learning.taskmanager.event.*;
import com.learning.taskmanager.parser.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Task Manager started");
        InMemoryTaskRepository repo = InMemoryTaskRepository.getInstance();
        repo.clear();
        TaskApi api = DemoFactory.createOfflineApi();
        TaskService service = new TaskService(repo, new RuleBasedTaskParser(), new RuleBasedBreakdownStrategy());
        ConsoleLogListener log = new ConsoleLogListener();
        AuditTrailListener audit = new AuditTrailListener();
        service.addListener(log);
        service.addListener(audit);
        TaskRequest rent = new TaskRequest("Pay rent", "Monthly rent", "false", "2026-12-01", "HIGH");
        System.out.println(api.createTask(rent, "rent-key"));
        System.out.println(api.createTask(rent, "rent-key"));
        System.out.println(api.createTask(new TaskRequest("", "", "maybe", "2026-02-30", "urgent"), null));
        System.out.println(api.listTasks(TaskQuery.builder().search("rent").sortBy("title").page(0).size(5).build()));


        // --------------------------------------------------
// LLM DEMO 1: Create a task from natural language
// --------------------------------------------------

        ApiResponse llmCreateResponse = api.createTaskFromText(
                "Remind me to prepare the project demo by December 5, high priority",
                "llm-create-demo-001"
        );

        System.out.println(llmCreateResponse);


// --------------------------------------------------
// LLM DEMO 2: Break down a task into subtasks
// --------------------------------------------------

// In your current demo, the first regular task has ID 1.
// The LLM-created task above will likely have ID 2.
// Check the create response and use the actual task ID.

        int taskIdForBreakdown = 2;

        ApiResponse breakdownResponse = api.breakDownTask(
                taskIdForBreakdown,
                "llm-breakdown-demo-001"
        );

        System.out.println(breakdownResponse);


// --------------------------------------------------
// LLM DEMO 3: List the generated subtasks
// --------------------------------------------------

        ApiResponse subtasksResponse = api.listSubtasks(
                taskIdForBreakdown
        );

        System.out.println(subtasksResponse);



        System.out.println("Repository before parent deletion: " + repo.findAll().size());
        api.createSubtask(1, new TaskRequest("Transfer money", "Pay via bank", "false", null, "MEDIUM"));
        api.deleteTask(1);
        System.out.println("Repository after parent deletion: " + repo.findAll().size());
        service.removeListener(log);
        System.out.println("Audit events: " + audit.getCounts());
    }
}
