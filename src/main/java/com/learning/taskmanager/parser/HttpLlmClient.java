package com.learning.taskmanager.parser;

import com.learning.taskmanager.exception.ServiceUnavailableException;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class HttpLlmClient implements LlmClient {
    private String key;

    public HttpLlmClient(String key) {
        this.key = key;
    }

    public String complete(String prompt) {
        if (key == null || key.isBlank()) throw new ServiceUnavailableException("LLM_API_KEY is not configured");
        try {
            String payload = "{\"model\":\"gpt-4o-mini\",\"temperature\":0.2,\"messages\":[{\"role\":\"user\",\"content\":\"" + escape(prompt) + "\"}]}";
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/chat/completions")).timeout(Duration.ofSeconds(30)).header("Authorization", "Bearer " + key).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(payload)).build();
            HttpResponse<String> res = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300)
                throw new RuntimeException("LLM HTTP status " + res.statusCode());
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\\"content\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*?)\\\"", java.util.regex.Pattern.DOTALL).matcher(res.body());
            if (!m.find()) throw new RuntimeException("Missing LLM content");
            return unescape(m.group(1));
        } catch (ServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("LLM request failed", e);
        }
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String unescape(String s) {
        return s.replace("\\n", "\n").replace("\\r", "").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
