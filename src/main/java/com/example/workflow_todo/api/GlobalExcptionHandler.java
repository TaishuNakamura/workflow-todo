package com.example.workflow_todo.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/*
    例外をJSONで返す。
    ControllerやServiceで　throw new ApiExceptionをしたら、
    Springが自動でcatchして、決めた形式のJSONで返す。
 */
@RestControllerAdvice
public class GlobalExcptionHandler {
    
    /*
        ResponseEntity ... HTTPレスポンスの入れ物。
        ResponseEntity<ErrorResponse> ... 中身(body)がErrorResponseであることを型で宣言。
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex){
        HttpStatus status;
        switch (ex.getCode()){
            case NOT_FOUND:
                status = HttpStatus.NOT_FOUND;
                break;
            case VALIDATION_ERROR:
                status = HttpStatus.BAD_REQUEST;
                break;
            case INVALID_STATE:
            case CHILDREN_INCOMPLETE:
                status = HttpStatus.CONFLICT;
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        /*
            ResponseBody(JSONの元)の作成。
            recordの値(code/message/details)を持つ。
         */
        ErrorResponse body = new ErrorResponse(ex.getCode(), ex.getMessage(), ex.getDetails());
        return ResponseEntity.status(status).body(body);
    }
}
