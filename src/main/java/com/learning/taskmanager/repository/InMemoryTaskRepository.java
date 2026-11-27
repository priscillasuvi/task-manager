package com.learning.taskmanager.repository;

import com.learning.taskmanager.model.Task;

import java.util.*;

public final class InMemoryTaskRepository implements TaskRepository {
    private static final InMemoryTaskRepository INSTANCE = new InMemoryTaskRepository();
    private final Map<Integer, Task> tasks = new LinkedHashMap<>();
    private int nextId = 1;

    private InMemoryTaskRepository() {
    }

    public static InMemoryTaskRepository getInstance() {
        return INSTANCE;
    }

    public synchronized Task save(Task t) {
        Task saved = t.toBuilder().id(t.getId() > 0 ? t.getId() : nextId++).build();
        tasks.put(saved.getId(), saved);
        return saved;
    }

    public synchronized Optional<Task> findById(int id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public synchronized List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    public synchronized void delete(int id) {
        tasks.remove(id);
    }

    public synchronized void clear() {
        tasks.clear();
        nextId = 1;
    }
}
