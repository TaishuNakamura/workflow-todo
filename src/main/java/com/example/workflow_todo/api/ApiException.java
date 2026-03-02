package com.example.workflow_todo.api;

/*
 Service層からthrowできる、code/message/details を持つ例外を用意する。 
 */
public class ApiException extends RuntimeException {
    private final ErrorCode code;   // enumで固定
    private final Object details;

    // detailsが不要な場合
    public ApiException(ErrorCode code, String message){
        this(code, message, null);
    }

    // detailsが必要な場合
    public ApiException(ErrorCode code, String message, Object details){
        super(message);     // 例外の標準メッセージとして保持
        this.code = code;
        this.details = details;
    }

    public ErrorCode getCode(){
        return code;
    }

    public Object getDetails(){
        return details;
    }

    public static ApiException validationError(Object details){
        return new ApiException(ErrorCode.VALIDATION_ERROR, null, details);
    }

    public static ApiException childrenIncomplete(Object details){
        return new ApiException(ErrorCode.CHILDREN_INCOMPLETE, null, details);
    }

    public static ApiException invalidState(){
        return new ApiException(ErrorCode.INVALID_STATE, null);
    }

    public static ApiException notFound(){
        return new ApiException(ErrorCode.NOT_FOUND, null);
    }
}
