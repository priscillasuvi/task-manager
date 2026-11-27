package com.learning.taskmanager.parser;

import com.learning.taskmanager.model.ParsedTask;
import com.learning.taskmanager.exception.BadGatewayException;
import com.learning.taskmanager.exception.ServiceUnavailableException;

public class LlmTaskParser implements TaskParser {
    private final LlmClient client;

    public LlmTaskParser(LlmClient c) {
        client = c;
    }

    public ParsedTask parse(String text, String today) {
        String prompt = "Today is " + today + ". Convert this request into JSON only with string fields title, description, dueDate (YYYY-MM-DD or null), recurrence (or null). Do not invent a date. Request: " + text;
        try {
            var m = SimpleJson.parseObject(client.complete(prompt));
            return new ParsedTask(m.get("title"), m.get("description"), m.get("dueDate"), m.get("recurrence"));
        } catch (ServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new BadGatewayException("LLM returned invalid task data", e);
        }
    }
}
