package com.learning.taskmanager.model;

public final class Task {
    private final int id;
    private final String title, description, dueDate, recurrence;
    private final boolean completed;
    private final Priority priority;
    private final Integer parentId;

    private Task(Builder b) {
        id = b.id;
        title = b.title;
        description = b.description;
        completed = b.completed;
        dueDate = b.dueDate;
        priority = b.priority;
        parentId = b.parentId;
        recurrence = b.recurrence;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getDueDate() {
        return dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public Integer getParentId() {
        return parentId;
    }

    public String getRecurrence() {
        return recurrence;
    }

    public Builder toBuilder() {
        return new Builder().id(id).title(title).description(description).completed(completed)
                .dueDate(dueDate).priority(priority).parentId(parentId).recurrence(recurrence);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private int id;
        private String title, description, dueDate, recurrence;
        private boolean completed;
        private Priority priority = Priority.MEDIUM;
        private Integer parentId;

        public Builder id(int v) {
            id = v;
            return this;
        }

        public Builder title(String v) {
            title = v;
            return this;
        }

        public Builder description(String v) {
            description = v;
            return this;
        }

        public Builder completed(boolean v) {
            completed = v;
            return this;
        }

        public Builder dueDate(String v) {
            dueDate = v;
            return this;
        }

        public Builder priority(Priority v) {
            priority = v;
            return this;
        }

        public Builder parentId(Integer v) {
            parentId = v;
            return this;
        }

        public Builder recurrence(String v) {
            recurrence = v;
            return this;
        }

        public Task build() {
            if (title == null || description == null) throw new IllegalStateException("title and description required");
            return new Task(this);
        }
    }
}
