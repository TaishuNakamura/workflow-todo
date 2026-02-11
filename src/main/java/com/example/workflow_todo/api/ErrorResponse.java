package com.example.workflow_todo.api;

public record ErrorResponse(
    ErrorCode code,
    String message,
    Object details
) {}
