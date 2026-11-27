package com.learning.taskmanager.model;

public class TaskQuery {
    public final String search, status, priority, sortBy;
    public final int page, size;

    private TaskQuery(Builder b) {
        search = b.search;
        status = b.status;
        priority = b.priority;
        sortBy = b.sortBy;
        page = b.page;
        size = b.size;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String search, status, priority, sortBy = "id";
        private int page = 0, size = 10;

        public Builder search(String v) {
            search = v;
            return this;
        }

        public Builder status(String v) {
            status = v;
            return this;
        }

        public Builder priority(String v) {
            priority = v;
            return this;
        }

        public Builder sortBy(String v) {
            sortBy = v;
            return this;
        }

        public Builder page(int v) {
            page = v;
            return this;
        }

        public Builder size(int v) {
            size = v;
            return this;
        }

        public TaskQuery build() {
            return new TaskQuery(this);
        }
    }
}
