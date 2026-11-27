package com.learning.taskmanager.parser;

import com.learning.taskmanager.model.ParsedTask;

import java.time.LocalDate;

public class RuleBasedTaskParser implements TaskParser {
    public ParsedTask parse(String text, String today) {
        String t = text.trim();
        String due = null, rec = null;
        try {
            LocalDate d = LocalDate.parse(today);
            if (t.toLowerCase().contains("tomorrow")) {
                due = d.plusDays(1).toString();
                t = t.replaceAll("(?i)\\btomorrow\\b", "").trim();
            } else if (t.toLowerCase().contains("today")) {
                due = d.toString();
                t = t.replaceAll("(?i)\\btoday\\b", "").trim();
            }
        } catch (Exception ignored) {
        }
        if (t.toLowerCase().contains("every month")) {
            rec = "monthly";
            t = t.replaceAll("(?i)every month", "").trim();
        }
        if (t.toLowerCase().contains("1st")) {
            if (due == null) due = today.substring(0, 7) + "-01";
            rec = rec == null ? "monthly" : rec;
            t = t.replace("1st", "").trim();
        }
        t = t.replaceAll("(?i)^remind me to\\s*", "").replaceAll("[.!]$", "").trim();
        return new ParsedTask(t, t, due, rec);
    }
}
