## DTO項目表

|API|Request DTO|Reqest項目(要約)|Response DTO|Response項目(要約)|主なエラー(ErrorResponse code)|備考|
|---|---|---|---|---|---|---|
|POST /tasks|CreateTaskRequest|title/parentId/purposeNote/priority/dueDate|TaskResponse|id/parentId/status/title/purposeNote/progressNote/waitingReason/priority/dueDate/createdAt/updatedAt|VALIDATION_ERROR, NOT_FOUND|status/createdAt/updatedAt/progressNote/waitingReasonはRequestで受け取らない|
|PATCH /tasks/{id}|UpdateTaskRequest|title/purposeNote/priority/dueDate/progressNote/waitingReason|TaskResponse|id/parentId/status/title/purposeNote/progressNote/waitingReason/priority/dueDate/createdAt/updatedAt|VALIDATION_ERROR, NOT_FOUND, INVALID_STATE|状態で更新項目が変わる/null送信ルール(dueDateのみnullで解除可、他はnullは400)/未指定は更新なし|
|POST /tasks/{id}/suspend|SuspendTaskRequest|progressNote|TaskResponse|id/parentId/status/title/purposeNote/progressNote/waitingReason/priority/dueDate/createdAt/updatedAt|VALIDATION_ERROR, NOT_FOUND, INVALID_STATE|progressNote必須、状態はNORMAL→SUSPENDED|
|POST /tasks/{id}/send-to-waiting|SendToWaitingRequest|waitingReason|TaskResponse|id/parentId/status/title/purposeNote/progressNote/waitingReason/priority/dueDate/createdAt/updatedAt|VALIDATION_ERROR, NOT_FOUND, INVALID_STATE|waitingReason必須、状態はNORMAL→WAITING_REVIEW|
|GET /tasks|なし|なし|TaskResponse[]|id/parentId/status/title/purposeNote/progressNote/waitingReason/priority/dueDate/createdAt/updatedAt|なし|ソート条件：priority 高→低、同率はcreatedAt降順|
|GET /tasks/waiting|なし|なし|TaskResponse[]|id/parentId/status/title/purposeNote/progressNote/waitingReason/priority/dueDate/createdAt/updatedAt|なし|WAITING_REVIEWのみ、ソート条件：priority 高→低、同率はcreatedAt降順|
|GET /tasks/{id}|なし|なし|TaskDetailResponse|task/children|NOT_FOUND|親IDのとき、childrenを含む/子IDのとき、childrenフィールドなし|
|POST /tasks/{id}/resume|なし|なし|TaskResponse|id/parentId/status/title/purposeNote/progressNote/waitingReason/priority/dueDate/createdAt/updatedAt|NOT_FOUND, INVALID_STATE|SUSPENDED→NORMAL|
|POST /tasks/{id}/reject|なし|なし|TaskResponse|id/parentId/status/title/purposeNote/progressNote/waitingReason/priority/dueDate/createdAt/updatedAt|NOT_FOUND, INVALID_STATE|WAITING_REVIEW→NORMAL|
|POST /tasks/{id}/approve|なし|なし|TaskResponse|id/parentId/status/title/purposeNote/progressNote/waitingReason/priority/dueDate/createdAt/updatedAt|NOT_FOUND, INVALID_STATE, CHILDREN_INCOMPLETE|WAITING_REVIEW→DONE|
|POST /tasks/{id}/complete|なし|なし|TaskResponse|id/parentId/status/title/purposeNote/progressNote/waitingReason/priority/dueDate/createdAt/updatedAt|NOT_FOUND, INVALID_STATE, CHILDREN_INCOMPLETE|NORMAL→DONE|