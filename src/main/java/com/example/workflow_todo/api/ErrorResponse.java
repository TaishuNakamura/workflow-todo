package com.example.workflow_todo.api;

/*
    共通エラーの返却フォーマット
    record ... 不変なデータキャリア専用のクラス構文。
            　　Request/Response DTOなどの用途に限定して使用可能。
 */
public record ErrorResponse(
    ErrorCode code,
    String message,
    Object details
) {}
