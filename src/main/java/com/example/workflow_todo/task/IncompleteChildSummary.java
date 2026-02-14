package com.example.workflow_todo.task;

public record IncompleteChildSummary(
    String id,
    String title,
    TaskStatus status
) {}
