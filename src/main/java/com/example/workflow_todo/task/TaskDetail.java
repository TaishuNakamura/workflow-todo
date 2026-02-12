package com.example.workflow_todo.task;

import java.time.OffsetDateTime;

public record TaskDetail(
    String id,
    String status,
    OffsetDateTime updatedAt
) {}
