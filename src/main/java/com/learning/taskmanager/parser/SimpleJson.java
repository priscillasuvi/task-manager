package com.learning.taskmanager.parser;

import java.util.*;
import java.util.regex.*;

public final class SimpleJson {
    private SimpleJson() {
    }

    public static Map<String, String> parseObject(String json) {
        if (json == null) throw new IllegalArgumentException("null JSON");
        String s = json.trim();
        if (s.startsWith("```")) {
            s = s.replaceAll("(?s)^```(?:json)?\\s*", "").replaceAll("(?s)\\s*```$", "").trim();
        }
        if (!s.startsWith("{") || !s.endsWith("}")) throw new IllegalArgumentException("Expected JSON object");
        Map<String, String> out = new HashMap<>();
        Matcher m = Pattern.compile("\\\"([A-Za-z]+)\\\"\\s*:\\s*(null|\\\"([^\\\"]*)\\\")").matcher(s);
        while (m.find())
            out.put(m.group(1), "null".equals(m.group(2)) ? null : m.group(3).replace("\\n", "\n").replace("\\\"", "\""));
        return out;
    }
}
