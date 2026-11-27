package com.learning.taskmanager.parser;

public class FakeLlmClient implements LlmClient {
    private final String reply;
    private int calls;

    public FakeLlmClient(String r) {
        reply = r;
    }

    public String complete(String p) {
        calls++;
        return reply;
    }

    public int getCalls() {
        return calls;
    }
}
