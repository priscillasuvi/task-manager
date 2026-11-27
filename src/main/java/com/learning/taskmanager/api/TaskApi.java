package com.learning.taskmanager.api;

import com.learning.taskmanager.model.*;
import com.learning.taskmanager.service.*;
import com.learning.taskmanager.exception.*;

public class TaskApi {
    private final TaskService service;

    public TaskApi(TaskService s) {
        service = s;
    }

    private ApiResponse call(java.util.function.Supplier<ApiResponse> f) {
        try {
            return f.get();
        } catch (ApiException e) {
            return ErrorResponseFactory.from(e);
        } catch (Exception e) {
            return ErrorResponseFactory.internal(e);
        }
    }

    private ApiResponse data(int status, String json) {
        return new ApiResponse(status, "{\"data\":" + json + "}");
    }

    public ApiResponse createTask(TaskRequest r, String k) {
        return call(() -> data(201, TaskService.taskJson(service.createAndStore(r, k))));
    }

    public ApiResponse getTask(int id) {
        return call(() -> data(200, TaskService.taskJson(service.get(id))));
    }

    public ApiResponse listTasks(TaskQuery q) {
        return call(() -> data(200, TaskService.tasksJson(service.query(q))));
    }

    public ApiResponse updateTask(int id, TaskRequest r) {
        return call(() -> data(200, TaskService.taskJson(service.update(id, r))));
    }

    public ApiResponse completeTask(int id) {
        return call(() -> data(200, TaskService.taskJson(service.complete(id))));
    }

    public ApiResponse deleteTask(int id) {
        return call(() -> {
            service.delete(id);
            return data(200, "{\"deleted\":true}");
        });
    }

    public ApiResponse createSubtask(int id, TaskRequest r) {
        return call(() -> data(201, TaskService.taskJson(service.subtask(id, r))));
    }

    public ApiResponse listSubtasks(int id) {
        return call(() -> data(200, TaskService.tasksJson(service.subtasks(id))));
    }

    public ApiResponse createTaskFromText(String text, String key) {
        return call(() -> data(201, TaskService.taskJson(service.createFromText(text, key))));
    }

    public ApiResponse breakDownTask(int id, String key) {
        return call(() -> data(201, TaskService.tasksJson(service.breakDown(id, key))));
    }
}
