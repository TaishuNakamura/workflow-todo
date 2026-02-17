package com.example.workflow_todo.task;

import java.time.OffsetDateTime;

/*
    APIのレスポンス用の写しでクライアントに返す形式に整えたもの。
 */
public record TaskDetail(
    String id,
    String title,
    TaskStatus status,
    OffsetDateTime updatedAt
) {}
