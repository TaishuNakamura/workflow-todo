package com.example.workflow_todo.task;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import com.example.workflow_todo.api.ApiException;
import com.example.workflow_todo.api.ErrorCode;

/*
    Serviceクラス。(仕様・ルールを実行)
 */
@Service
public class TaskService {
    
    public TaskDetail resume(String id){
        // 仮ルール
        if("404".equals(id)){
            throw new ApiException(ErrorCode.NOT_FOUND, "タスクが見つかりません");
        }
        if("409".equals(id)){
            throw new ApiException(ErrorCode.INVALID_STATE, "タスク状態が不正です");
        }
        return new TaskDetail(id, "NORMAL", OffsetDateTime.now());
    }
}
