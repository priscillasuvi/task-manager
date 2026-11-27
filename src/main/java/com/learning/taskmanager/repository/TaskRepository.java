package com.learning.taskmanager.repository;

import com.learning.taskmanager.model.Task;

import java.util.*;

public interface TaskRepository {
    Task save(Task t);

    Optional<Task> findById(int id);

    List<Task> findAll();

    void delete(int id);
}
