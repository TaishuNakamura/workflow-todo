package com.example.workflow_todo.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.workflow_todo.api.ApiException;
import com.example.workflow_todo.api.ErrorCode;

/*
    Serviceクラス。(仕様・ルールを実行)
    TaskServiceは「操作の受付窓口」
    「操作の許可/不許可、親子制約、状態遷移」を判断し、Taskを更新してDTOを返す。
 */
@Service
public class TaskService {
    
    private final Map<String, Task> store = new ConcurrentHashMap<>();

    // コンストラクタ
    public TaskService(){
        // 仮データ
        store.put("P1", new Task("P1", TaskStatus.SUSPENDED));
        store.put("C1", new Task("C1", "P1", TaskStatus.SUSPENDED));

        store.put("P2", new Task("P2", TaskStatus.NORMAL));
        store.put("C2", new Task("C2", "P2", TaskStatus.SUSPENDED));
    }

    public TaskDetail resume(String id){
        Task task = store.get(id);
        if(task == null){
            throw new ApiException(ErrorCode.NOT_FOUND, "タスクが見つかりません。");
        }

        String parentId = task.getParentId();
        if(parentId != null){
            Task parent = store.get(parentId);
            if(parent != null && (parent.getStatus() == TaskStatus.SUSPENDED || parent.getStatus() == TaskStatus.DONE)){
                throw new ApiException(ErrorCode.INVALID_STATE, "タスク状態が不正です。");
            }
        }

        if(task.getStatus() != TaskStatus.SUSPENDED){
            throw new ApiException(ErrorCode.INVALID_STATE, "タスク状態が不正です。");
        }

        task.setStatus(TaskStatus.NORMAL);
        return new TaskDetail(task.getId(), task.getStatus(), task.getUpdatedAt());
    }
}
