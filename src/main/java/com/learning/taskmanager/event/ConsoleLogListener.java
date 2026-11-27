package com.learning.taskmanager.event;

import com.learning.taskmanager.model.Task;

public class ConsoleLogListener implements TaskEventListener {
    public void onEvent(String e, Task t) {
        System.out.println("[EVENT] " + e + " taskId=" + t.getId());
    }
}
