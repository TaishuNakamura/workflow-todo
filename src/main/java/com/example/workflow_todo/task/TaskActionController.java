package com.example.workflow_todo.task;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

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

    // POST task
    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDetail create(@RequestBody CreateTaskRequest body){
        String title = body.title();

        if(title == null || title.isBlank()){
            Map<String, Object> details = Map.of("fields", List.of(Map.of("name", "title", "reason", "空入力は禁止。")));
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "入力値が不正です。", details);
        }

        // parentId が空文字なら null
        String parentId = body.parentId();
        if(parentId != null && parentId.isBlank()){
            parentId = null;
        }

        return taskService.create(title, parentId);
    }

    // PATCH title
    @PatchMapping("/tasks/{id}/title")
    public TaskDetail rename(@PathVariable String id, @RequestBody(required = false) String body){
        return taskService.rename(id, body);
    }

    // GET tasks/{id}  1件のタスクの取得
    @GetMapping("/tasks/{id}")
    public TaskDetail get(@PathVariable String id){
        return taskService.getTask(id);
    }

    // GET tasks
    @GetMapping("/tasks")
    public List<TaskDetail> list(@RequestParam(required = false) String parentId){
        return taskService.listAll();
    }
}
