package com.example.workflow_todo.task;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public TaskDetail suspend(@PathVariable String id){
        return taskService.suspend(id);
    }

    // POST send-to-waiting
    @PostMapping("/tasks/{id}/send-to-waiting")
    public TaskDetail sendToWaiting(@PathVariable String id){
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
}
