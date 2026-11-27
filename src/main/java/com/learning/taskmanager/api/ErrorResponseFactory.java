package com.learning.taskmanager.api;

import com.learning.taskmanager.exception.*;
import com.learning.taskmanager.model.ApiResponse;

public final class ErrorResponseFactory {
    private ErrorResponseFactory() {
    }

    public static ApiResponse from(ApiException e) {
        StringBuilder d = new StringBuilder("[");
        if (e instanceof ValidationException v) {
            for (int i = 0; i < v.getIssues().size(); i++) {
                String[] a = v.getIssues().get(i);
                if (i > 0) d.append(',');
                d.append("{\"field\":\"").append(esc(a[0])).append("\",\"issue\":\"").append(esc(a[1])).append("\"}");
            }
        }
        d.append(']');
        return new ApiResponse(e.getStatusCode(), "{\"error\":{\"code\":\"" + esc(e.getErrorCode()) + "\",\"message\":\"" + esc(e.getMessage()) + "\",\"details\":" + d + "}}");
    }

    public static ApiResponse internal(Exception e) {
        return new ApiResponse(500, "{\"error\":{\"code\":\"INTERNAL_ERROR\",\"message\":\"Internal error\",\"details\":[]}}");
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}
