package com.learning.taskmanager.app;

import com.learning.taskmanager.api.*;
import com.learning.taskmanager.repository.*;
import com.learning.taskmanager.parser.*;
import com.learning.taskmanager.service.*;

public final class DemoFactory {
    private DemoFactory() {
    }

    public static TaskApi createOfflineApi() {
        return create(new RuleBasedTaskParser(), new RuleBasedBreakdownStrategy());
    }

    public static TaskApi create(TaskParser p, BreakdownStrategy b) {
        return new TaskApi(new TaskService(InMemoryTaskRepository.getInstance(), p, b));
    }

    public static TaskApi createAiApi() {
        String key = System.getenv("LLM_API_KEY");
        LlmClient client = new HttpLlmClient(key);
        return create(new LlmTaskParser(client), new LlmBreakdownStrategy(client));
    }
}
