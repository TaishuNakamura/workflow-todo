package com.example.workflow_todo.task;

import java.time.OffsetDateTime;

/*
    Serviceが扱う"タスクという概念の本体"。
    ※TaskServiceは「操作のルール」、Taskは「状態そのものと状態変更の一貫性」を持つ。
 */
public class Task {
    private final String id;
    private final String parentId;
    private String title;
    private TaskStatus status;
    private OffsetDateTime updatedAt;

    // コンストラクタ(親IDあり)
    public Task(String id, String parentId, String title, TaskStatus status){
        this.id = id;
        this.parentId = parentId;
        this.title = title;
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }
    // コンストラクタ(親IDなし)
    public Task(String id, String title, TaskStatus status){
        this(id, null, title, status);
    }
    
    // getter
    public String getId(){ return id; }
    public String getParentId(){ return parentId; }
    public String getTitle(){ return title; }
    public TaskStatus getStatus(){ return status; }
    public OffsetDateTime getUpdatedAt(){ return updatedAt; }

    // setter
    public void setStatus(TaskStatus status){
        this.status = status;
        this.updatedAt = OffsetDateTime.now();
    }

    public void setTitle(String title){
        this.title = title;
        this.updatedAt = OffsetDateTime.now();
    }
}
