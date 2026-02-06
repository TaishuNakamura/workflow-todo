# API一覧表

|API|目的|対象|リクエストBody|成功レスポンス|主なエラー|備考|
|---|---|---|---|---|---|---|
|GET /tasks|タスクの一覧取得|親/子とも(フラット)|なし|タスク一覧(親子混在)|なし|ソート：priority 高→低、同率はcreatedAt 降順|
|GET /tasks/waiting|承認待ちのタスク一覧取得|親/子とも(フラット)|なし|WAITING_REVIEWのタスク一覧(親子混在)|なし|ソート：priority 高→低、同率はcreatedAt 降順|
|GET /tasks/{id}|タスクの詳細取得|親/子とも|なし|タスクの詳細(親の場合は子タスク配列を含む)|404 NOT_FOUND|子を含むのは親の時だけ|
|POST /tasks|タスクの作成|親/子とも|title：必須<br>parentId：親/子判定<br>purposeNote：親必須/子任意<br>priority：任意(未指定MED補完)<br>dueDate：任意(未指定null)|作成したタスク|400 VALIDATION_ERROR<br>404 NOT_FOUND|status/createdAt/updatedAt/progressNote/waitingReason を送ったら 400<br>孫タスクの作成禁止|
|PATCH /tasks/{id}|タスク項目の更新|親/子とも|更新候補項目(title/purposeNote/priority/dueDate/progressNote/waitingReason)<br>※状態により更新可能項目が変わる|更新後タスク|400 VALIDATION_ERROR(禁止項目や空文字など)<br>404 NOT_FOUND<br>409 INVALID_STATE(状態に合わない更新)|更新可能項目は、<br>NORMAL：title, purposeNote, priority, dueDate<br>SUSPENDED：progressNote<br>WAITING_REVIEW：waitingReason<br>DONE：更新不可(409 INVALID_STATE)|
|POST /tasks/{id}/suspend|NORMAL→SUSPENDEDへの遷移|親/子とも|progressNote必須(null/空/トリム後空NG)|更新後タスク|400 VALIDATION_ERROR(progressNote不正など)<br>404 NOT_FOUND<br>409 INVALID_STATE(DONEで操作、状態不一致、親がSUSPENDED/DONEで子操作不可 等)|なし|
|POST /tasks/{id}/resume|SUSPENDED→NORMALへの遷移|親/子とも|なし|更新後タスク|404 NOT_FOUND<br>409 INVALID_STATE(状態不一致、DONEで操作、親がSUSPENDED/DONEで子操作不可など)|なし|
|POST /tasks/{id}/send-to-waiting|NORMAL→WAITING_REVIEWへの遷移|親/子とも|waitingReason 必須(null/空/トリム後空NG)|更新後タスク|400 VALIDATION_ERROR(waitingReason不正など)<br>404 NOT_FOUND<br>409 INVALID_STATE(状態不一致、DONEで操作、親がSUSPENDED/DONEで操作不可 等)|なし|
|POST /tasks/{id}/reject|WAITING_REVIEW→NORMALへの遷移|親/子とも|なし|更新後タスク|404 NOT_FOUND<br>409 INVALID_STATE(状態不一致、DONEで操作、親がSUSPENDED/DONEで子操作不可 等)|なし|
|POST /tasks/{id}/approve|WAITING_REVIEW→DONEへの遷移|親/子とも|なし|更新後タスク|404 NOT_FOUND<br>409 INVALID_STATE(状態不一致、DONEで操作、親がSUSPENDED/DONEで子操作不可 等)<br>409 CHILDREN_INCOMPLETE(子タスクがある場合、全子タスクがDONE必須)|なし|
|POST /tasks/{id}/complete|NORMAL→DONEへの遷移|親/子とも|なし|更新後タスク|404 NOT_FOUND<br>409 INVALID_STATE(状態不一致、DONEで操作、親がSUSPENDED/DONEで子操作不可 等)<br>409 CHILDREN_INCOMPLETE(子タスクがある場合、全子タスクがDONE必須)|なし|

### 注記
- 業務エラー(ErrorResponse)は原則 400/404/409。想定外は500。
- 親がSUSPENDEDまたはDONEの場合、子タスクへの操作は不可(409 INVALID_STATE)