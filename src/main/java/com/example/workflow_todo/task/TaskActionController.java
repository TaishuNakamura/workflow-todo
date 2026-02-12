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
    
    @PostMapping("/tasks/{id}/resume")
    public TaskDetail resume(@PathVariable String id){
        return taskService.resume(id);
    }
}
