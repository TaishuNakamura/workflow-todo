package com.example.workflow_todo.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        // 単体タスク
        store.put("T-N",  new Task("T-N",  TaskStatus.NORMAL));         // suspend, send-to-waiting, complete
        store.put("T-S",  new Task("T-S",  TaskStatus.SUSPENDED));      // resume
        store.put("T-W",  new Task("T-W",  TaskStatus.WAITING_REVIEW)); // approve, reject
        store.put("T-D",  new Task("T-D",  TaskStatus.DONE));           // INVALID_STATE確認用（任意）

        // 親子制約（親がSUSPENDEDなら子操作禁止）
        store.put("P-S", new Task("P-S", TaskStatus.SUSPENDED));
        store.put("C-S", new Task("C-S", "P-S", TaskStatus.NORMAL)); // 親SUSPENDEDなので子の操作は基本409

        // 親子制約（親がDONEなら子操作禁止）
        store.put("P-D", new Task("P-D", TaskStatus.DONE));
        store.put("C-D", new Task("C-D", "P-D", TaskStatus.NORMAL)); // 親DONEなので子の操作は基本409

        // CHILDREN_INCOMPLETE（approve用：親WAITING_REVIEW）
        store.put("P-A", new Task("P-A", TaskStatus.WAITING_REVIEW)); // approve対象
        store.put("C-A", new Task("C-A", "P-A", TaskStatus.NORMAL));  // 未完了 → approveでCHILDREN_INCOMPLETE

        // CHILDREN_INCOMPLETE（complete用：親NORMAL）
        store.put("P-C", new Task("P-C", TaskStatus.NORMAL));        // complete対象
        store.put("C-C", new Task("C-C", "P-C", TaskStatus.NORMAL)); // 未完了 → completeでCHILDREN_INCOMPLETE

        // 親子：正常に完了できる親（子がDONE済み）
        store.put("P-OK", new Task("P-OK", TaskStatus.NORMAL));
        store.put("C-OK", new Task("C-OK", "P-OK", TaskStatus.DONE)); // これで P-OK は complete 成功可能
    }

    // 状態：SUSPENDED->NORMAL
    public TaskDetail resume(String id){
        // 指定されたidからタスクの取得とNOT_FOUND判定
        Task task = getRequiredTask(id);

        // 親子制約：親がSUSPENDEDかDONEの子は操作禁止
        checkParentState(task);

        // 状態がSUSPENDED以外
        if(task.getStatus() != TaskStatus.SUSPENDED){
            throw new ApiException(ErrorCode.INVALID_STATE, "タスク状態が不正です。");
        }

        task.setStatus(TaskStatus.NORMAL);
        return new TaskDetail(task.getId(), task.getStatus(), task.getUpdatedAt());
    }

    // 状態：NORMAL->SUSPENDED
    public TaskDetail suspend(String id){
        // 指定されたidからタスクの取得とNOT_FOUND判定
        Task task = getRequiredTask(id);

        // 親子制約：親がSUSPENDEDかDONEの子は操作禁止
        checkParentState(task);

        // 状態がNORMALかWAITING_REVIEWならSUSPEND以外
        TaskStatus status = task.getStatus();
        if(status != TaskStatus.NORMAL && status != TaskStatus.WAITING_REVIEW){
            throw new ApiException(ErrorCode.INVALID_STATE, "タスク状態が不正です。");
        }

        task.setStatus(TaskStatus.SUSPENDED);
        return new TaskDetail(task.getId(), task.getStatus(), task.getUpdatedAt());
    }

    // 状態：NORMAL->WAITING_REVIEW
    public TaskDetail sendToWaiting(String id){
        // 指定されたidからタスクの取得とNOT_FOUND判定
        Task task = getRequiredTask(id);

        // 親子制約：親がSUSPENDEDかDONEの子は操作禁止
        checkParentState(task);

        // 状態がNORMAL以外
        if(task.getStatus() != TaskStatus.NORMAL){
            throw new ApiException(ErrorCode.INVALID_STATE, null);
        }
        
        task.setStatus(TaskStatus.WAITING_REVIEW);
        return new TaskDetail(task.getId(), task.getStatus(), task.getUpdatedAt());
    }

    // 状態：WAITING_REVIEW->NORMAL
    public TaskDetail reject(String id){
        // 指定されたidからタスクの取得とNOT_FOUND判定
        Task task = getRequiredTask(id);

        // 親子制約：親がSUSPENDEDかDONEの子は操作禁止
        checkParentState(task);

        // 状態がWAITING_REVIEW以外
        if(task.getStatus() != TaskStatus.WAITING_REVIEW){
            throw new ApiException(ErrorCode.INVALID_STATE, null);
        }

        task.setStatus(TaskStatus.NORMAL);
        return new TaskDetail(task.getId(), task.getStatus(), task.getUpdatedAt());
    }

    // 状態：WAITING_REVIEW
    public TaskDetail approve(String id){
        // 指定されたidからタスクの取得とNOT_FOUND判定
        Task task = getRequiredTask(id);

        // 親子制約：親がSUSPENDEDかDONEの子は操作禁止
        checkParentState(task);

        // WAITING_REVIEW以外
        if(task.getStatus() != TaskStatus.WAITING_REVIEW){
            throw new ApiException(ErrorCode.INVALID_STATE, null);
        }

        // 未完了の子タスクあり
        Map<String, Object> details = incompleteChildrenDetails(task.getId());
        List<?> list = (List<?>) details.get("incompleteChildren");
        if(!list.isEmpty()){
            throw new ApiException(ErrorCode.CHILDREN_INCOMPLETE, null, details);
        }

        task.setStatus(TaskStatus.DONE);
        return new TaskDetail(task.getId(), task.getStatus(), task.getUpdatedAt());
    }

    // 状態：NORMAL
    public TaskDetail complete(String id){
        // 指定されたidからタスクの取得とNOT_FOUND判定
        Task task = getRequiredTask(id);

        // 親子制約：親がSUSPENDEDかDONEの子は操作禁止
        checkParentState(task);

        // NORMAL以外
        if(task.getStatus() != TaskStatus.NORMAL){
            throw new ApiException(ErrorCode.INVALID_STATE, null);
        }

        // 未完了の子タスクあり
        Map<String, Object> details = incompleteChildrenDetails(task.getId());
        List<?> list = (List<?>) details.get("incompleteChildren");
        if(!list.isEmpty()){
            throw new ApiException(ErrorCode.CHILDREN_INCOMPLETE, null, details);
        }

        task.setStatus(TaskStatus.DONE);
        return new TaskDetail(task.getId(), task.getStatus(), task.getUpdatedAt());
    }

    
    // idからタスクを取得（ないならNOT_FOUND）
    private Task getRequiredTask(String id){
        Task task = store.get(id);
        if(task == null){
            throw new ApiException(ErrorCode.NOT_FOUND, null);
        }
        return task;
    }

    // 親子制約のチェック(親の状態チェック)
    private void checkParentState(Task task){
        String parentId = task.getParentId();
        if(parentId == null) return;

        Task parent = store.get(parentId);
        if(parent != null && (parent.getStatus() == TaskStatus.SUSPENDED || parent.getStatus() == TaskStatus.DONE)){
            throw new ApiException(ErrorCode.INVALID_STATE, null);
        }
    }

    // 未完了の子タスク取得
    private Map<String, Object> incompleteChildrenDetails(String parentId){
        List<IncompleteChildSummary> incompleteChildren = new ArrayList<>();

        for(Task t : store.values()){
            if(parentId.equals(t.getParentId()) && t.getStatus() != TaskStatus.DONE){
                incompleteChildren.add(new IncompleteChildSummary(t.getId(), t.getId(), t.getStatus()));
            }
        }

        Map<String, Object> details = new HashMap<>();
        details.put("incompleteChildren", incompleteChildren);
        return details;
    }
}
