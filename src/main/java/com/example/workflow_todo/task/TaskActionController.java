package com.example.workflow_todo.task;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.workflow_todo.api.ApiException;
import com.example.workflow_todo.api.ErrorCode;
import com.example.workflow_todo.api.ValidationFieldError;


/*
    Controllerクラス。(HTTPの窓口的役割)
 */
@RestController
public class TaskActionController {

    private final TaskService taskService;

    public TaskActionController(TaskService taskService){
        this.taskService = taskService;
    }
    
    // POST resume
    @PostMapping("/tasks/{id}/resume")
    public TaskDetail resume(@PathVariable String id){
        return taskService.resume(id);
    }

    // POST suspend
    @PostMapping("/tasks/{id}/suspend")
    public TaskDetail suspend(
        @PathVariable String id, 
        @RequestBody(required = false) SuspendRequest body){
        if(body == null || body.progressNote() == null || body.progressNote().isBlank()){
            throw new ApiException(ErrorCode.VALIDATION_ERROR, null, Map.of("fields", List.of(new ValidationFieldError("progressNote", "空入力は禁止。"))));
        }
        
        return taskService.suspend(id);
    }

    // POST send-to-waiting
    @PostMapping("/tasks/{id}/send-to-waiting")
    public TaskDetail sendToWaiting(
        @PathVariable String id, 
        @RequestBody(required = false) SendToWaitingRequest body
    ){    
        if(body == null || body.waitingReason() == null || body.waitingReason().isBlank()){
            throw new ApiException(ErrorCode.VALIDATION_ERROR, null, Map.of("fields", List.of(new ValidationFieldError("waitingReason", "空入力は禁止。"))));
        }
        
        return taskService.sendToWaiting(id);
    }

    // POST reject
    @PostMapping("/tasks/{id}/reject")
    public TaskDetail reject(@PathVariable String id){
        return taskService.reject(id);
    }

    // POST approve
    @PostMapping("/tasks/{id}/approve")
    public TaskDetail approve(@PathVariable String id){
        return taskService.approve(id);
    }

    // POST complete
    @PostMapping("/tasks/{id}/complete")
    public TaskDetail complete(@PathVariable String id){
        return taskService.complete(id);
    }
}
