package com.learning.taskmanager.parser;

public final class ParserFactory {
    private ParserFactory() {
    }

    public static TaskParser create(String key, LlmClient client) {
        return key == null || key.isBlank() ? new RuleBasedTaskParser() : new LlmTaskParser(client);
    }
}
