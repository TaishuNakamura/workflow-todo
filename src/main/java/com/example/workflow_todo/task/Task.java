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
    private final Priority priority;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // コンストラクタ(親IDあり)
    public Task(String id, String parentId, String title, TaskStatus status, Priority priority){
        this.id = id;
        this.parentId = parentId;
        this.title = title;
        this.status = status;
        this.priority = (priority == null) ? Priority.MED : priority; // 未指定の場合はMED

        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    // コンストラクタ(親IDあり),priority省略
    public Task(String id, String parentId, String title, TaskStatus status){
        this(id, parentId, title, status, null);
    }
    // コンストラクタ(親IDなし),priority省略
    public Task(String id, String title, TaskStatus status){
        this(id, null, title, status, null);
    }
    
    // getter
    public String getId(){ return id; }
    public String getParentId(){ return parentId; }
    public String getTitle(){ return title; }
    public TaskStatus getStatus(){ return status; }
    public Priority getPriority(){ return priority; }
    public OffsetDateTime getCreatedAt(){ return createdAt; }
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
