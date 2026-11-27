package com.learning.taskmanager.event;

import com.learning.taskmanager.model.Task;

public interface TaskEventListener {
    void onEvent(String eventType, Task task);
}
