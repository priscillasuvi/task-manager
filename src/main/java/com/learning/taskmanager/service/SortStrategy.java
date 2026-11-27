package com.learning.taskmanager.service;

import com.learning.taskmanager.model.Task;

import java.util.Comparator;

public interface SortStrategy {
    Comparator<Task> comparator();
}
