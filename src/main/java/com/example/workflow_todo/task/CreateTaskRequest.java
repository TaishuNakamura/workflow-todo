package com.example.workflow_todo.task;

public record CreateTaskRequest(
    String title, 
    String parentId,
    String priority
) {}
