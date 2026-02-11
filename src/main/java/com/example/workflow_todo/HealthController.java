package com.example.workflow_todo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.workflow_todo.api.ApiException;
import com.example.workflow_todo.api.ErrorCode;


/*
    動作テスト用コントローラー
 */
@RestController
public class HealthController {
    
    @GetMapping("/health")
    public String helth(){
        return "ok";
    }

    @GetMapping("/boom")
    public String boom(){
        throw new ApiException(ErrorCode.NOT_FOUND, "タスクが見つかりません。");
    }
}
