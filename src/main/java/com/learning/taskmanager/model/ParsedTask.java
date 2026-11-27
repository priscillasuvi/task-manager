package com.learning.taskmanager.model;

public class ParsedTask {
    private final String title, description, dueDate, recurrence;

    public ParsedTask(String t, String d, String due, String r) {
        title = t;
        description = d;
        dueDate = due;
        recurrence = r;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getRecurrence() {
        return recurrence;
    }
}
