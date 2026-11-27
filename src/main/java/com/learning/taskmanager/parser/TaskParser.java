package com.learning.taskmanager.parser;

import com.learning.taskmanager.model.ParsedTask;

public interface TaskParser {
    ParsedTask parse(String text, String today);
}
