package com.example.workflow_todo.task;

import java.time.OffsetDateTime;

public class Task {
    private final String id;
    private TaskStatus status;
    private OffsetDateTime updatedAt;

    public Task(String id, TaskStatus status){
        this.id = id;
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }
    
    public String getId(){ return id; }
    public TaskStatus getStatus(){ return status; }
    public OffsetDateTime getUpdatedAt(){ return updatedAt; }

    public void setStatus(TaskStatus status){
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }
}
