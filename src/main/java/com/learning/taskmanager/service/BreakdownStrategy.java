package com.learning.taskmanager.service;

import com.learning.taskmanager.model.Task;
import com.learning.taskmanager.model.TaskRequest;

import java.util.List;

public interface BreakdownStrategy {
    List<TaskRequest> breakdown(Task task);
}
