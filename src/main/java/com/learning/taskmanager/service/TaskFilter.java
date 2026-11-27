package com.learning.taskmanager.service;

import com.learning.taskmanager.model.Task;

public interface TaskFilter {
    boolean matches(Task task);
}
