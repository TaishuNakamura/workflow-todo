package com.example.workflow_todo.task;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


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
    @Operation(summary = "中断を解除する")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "INVALID_STATE",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/tasks/{id}/resume")
    public TaskDetail resume(@PathVariable String id){
        return taskService.resume(id);
    }

    // POST suspend
    @Operation(summary = "タスクを中断する")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR(progressNote必須)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "INVALID_STATE",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(summary = "タスクを確認待ちにする")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR(progressNote必須)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "INVALID_STATE",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(summary = "タスクを差し戻す")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "INVALID_STATE",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/tasks/{id}/reject")
    public TaskDetail reject(@PathVariable String id){
        return taskService.reject(id);
    }

    // POST approve
    @Operation(summary = "タスクを承認する")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "INVALID_STATE",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/tasks/{id}/approve")
    public TaskDetail approve(@PathVariable String id){
        return taskService.approve(id);
    }

    // POST complete
    @Operation(summary = "タスクを完了する")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "INVALID_STATE",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/tasks/{id}/complete")
    public TaskDetail complete(@PathVariable String id){
        return taskService.complete(id);
    }

    // POST task
    @Operation(summary = "タスクを作成する")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "409", description = "INVALID_STATE",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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

        // priorityのエラー処理
        Priority priority;
        String p = body.priority();

        if(p == null || p.isBlank()){
            priority = Priority.MED;
        }else {
            try {
                priority = Priority.valueOf(p);
            }catch(IllegalArgumentException e){
                // VALIDATION_ERRORをdetails.fieldsに
                Map<String, Object> details = Map.of("fields", List.of(Map.of("name", "priority", "reason", "LOW/MED/HIGHのいずれかを指定してください。")));
                throw new ApiException(ErrorCode.VALIDATION_ERROR, "入力値が不正です。", details);
            }
        }

        return taskService.create(title, parentId, priority);
    }

    // PATCH title
    @Operation(summary = "タスクのタイトルを変更する")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "400", description = "VALIDATION_ERROR(title必須)",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/tasks/{id}/title")
    public TaskDetail rename(@PathVariable String id, @RequestBody(required = false) String body){
        return taskService.rename(id, body);
    }

    // GET tasks/{id}  1件のタスクの取得
    @Operation(summary = "タスク詳細を取得する")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/tasks/{id}")
    public TaskDetail get(@PathVariable String id){
        return taskService.getTask(id);
    }

    // GET tasks
    @Operation(summary = "タスク一覧を取得する")
    @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/tasks")
    public List<TaskDetail> list(@RequestParam(required = false) String parentId){
        return taskService.listAll();
    }
}
