package com.learning.taskmanager.parser;

public class FaultyLlmClient implements LlmClient {
    public String complete(String p) {
        throw new RuntimeException("simulated timeout");
    }
}
