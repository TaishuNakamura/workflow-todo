package com.example.workflow_todo.api;

public record ValidationFieldError(
    String name,
    String reason
) {}
