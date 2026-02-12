package com.example.workflow_todo.task;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.workflow_todo.api.ApiException;
import com.example.workflow_todo.api.ErrorCode;

/*
    Serviceクラス。(仕様・ルールを実行)
 */
@Service
public class TaskService {
    
    private final Map<String, Task> store = new ConcurrentHashMap<>();

    public TaskService(){
        // 仮データ
        store.put("1", new Task("1", TaskStatus.SUSPENDED));  // resume成功例
        store.put("2", new Task("2", TaskStatus.NORMAL));     // resume失敗例
    }

    public TaskDetail resume(String id){
        Task task = store.get(id);
        if(task == null){
            throw new ApiException(ErrorCode.NOT_FOUND, "タスクが見つかりません。");
        }
        if(task.getStatus() != TaskStatus.SUSPENDED){
            throw new ApiException(ErrorCode.INVALID_STATE, "タスク状態が不正です。");
        }

        task.setStatus(TaskStatus.NORMAL);
        return new TaskDetail(task.getId(), task.getStatus().name(), task.getUpdatedAt());
    }
}
