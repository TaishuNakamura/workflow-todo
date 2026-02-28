# ワークフローToDo

## 概要
タスクの状態(進捗段階・停止理由)を可視化し、管理できるワークフロー型のToDoアプリ。

## 背景・目的
日常業務や学習において、以下の課題を解決することを主眼とする。

- 複数タスクを並行して進める際、全体の進捗段階が俯瞰しづらい。
- 承認・添削などの外的要因による待ちタスクの量や内訳が把握しづらい。
- 中断した作業を再開する際、「どこまで進んでいたか」を探すことから始まり作業効率が下がる。

## 想定ユーザー
タスクが多くなりがちな社会人、または複数の科目・教材をこなす学生。

## スコープ
### 対象とすること
- 個人利用のみ。
- タスクはすべてユーザー自身が作成する。
- UIは簡易なWeb UIを想定するが、業務ルールはバックエンドに集約する。
- 学習目的のため、最小スコープから開始する。

### 対象としないこと(やらないこと)
- チーム利用、権限管理。
- 通知、期限管理、優先度管理。
- 履歴管理、監査ログ。
- 子タスクの高い層構造(親子1階層まで)

## 機能要件
- タスクの作成・一覧表示・詳細表示ができる。
- タスクを中断・解除できる(中断時に進捗メモ必須)。
- タスクを確認待ちにできる(確認待ち遷移時に待ち理由必須)。
- 確認結果として OK / NG を登録できる(OK:完了 / NG:通常状態に戻る)。
- 子タスクを持つ親タスクは、全ての子タスクが完了している場合にのみ完了できる。

## 非機能要件(最低限)
- 個人利用を前提とする。
- 業務ルール(状態遷移・必須入力)はバックエンドで保証する。
- UIは簡易なWeb UIとし、業務ルールはUIに依存しない。
- 不正な状態遷移が行われた場合は、理由を明示して拒否する。

## 制約事項
- 本システムは個人利用を前提とし、複数ユーザーや権限管理は行わない。
- タスクは常に1つの状態をのみを持ち、複数状態の同時付与は行わない。
- 状態数は最小限とし、「内容整理」は独立した状態として扱わない。
- 親子タスクは1階層までとし、孫タスクは扱わない。
- UIは簡易的なWeb UIとし、業務ルールはバックエンドに集約する。
- 通知、期限管理、優先度管理は対象外とする。

## 業務ルール
### 状態：NORMAL
| 操作 | 可否 | 条件/エラー |
|---|---|---|
| suspend | OK | progressNote必須 |
| resume | NG | INVALID_STATE |
| send-to-waiting | OK | waitingReason必須 |
| approve | NG | INVALID_STATE |
| reject | NG | INVALID_STATE |
| complete | OK | 子タスクがある場合、全子タスク完了が必須 |

### 状態：SUSPENDED
| 操作 | 可否 | 条件/エラー |
|---|---|---|
| suspend | NG | INVALID_STATE |
| resume | OK | なし |
| send-to-waiting | NG | INVALID_STATE |
| approve | NG | INVALID_STATE |
| reject | NG | INVALID_STATE |
| complete | NG | INVALID_STATE |
### 状態：WAITING_REVIEW
| 操作 | 可否 | 条件/エラー |
|---|---|---|
| suspend | NG | INVALID_STATE |
| resume | NG | INVALID_STATE |
| send-to-waiting | NG | INVALID_STATE |
| approve | OK | 子タスクがある場合、全ての子タスク完了が必須 |
| reject | OK | なし |
| complete | NG | INVALID_STATE |

### 状態：DONE
| 操作 | 可否 | 条件/エラー |
|---|---|---|
| suspend | NG | INVALID_STATE |
| resume | NG | INVALID_STATE |
| send-to-waiting | NG | INVALID_STATE |
| approve | NG | INVALID_STATE |
| reject | NG | INVALID_STATE |
| complete | NG | INVALID_STATE |

## API仕様（エンドポイント）
- GET /tasks：通常一覧(NORMAL/SUSPENDED/WAITING_REVIEW 混在)
- GET /tasks/waiting：確認待ち一覧(WAITING_REVIEW)
- GET /tasks/{id}：詳細(子タスク1階層を含む)
- POST /tasks：作成(親/子)
- PATCH /tasks/{id}：更新(DONEは更新不可)
- POST /tasks/{id}/suspend：中断
- POST /tasks/{id}/resume：中断解除
- POST /tasks/{id}/send-to-waiting：確認待ちへ
- POST /tasks/{id}/approve：承認(OK)
- POST /tasks/{id}/reject：差し戻し(NG)
- POST /tasks/{id}/complete：完了

## エラー設計
- code enum：VALIDATION_ERROR/INVALID_STATE/CHILDREN_INCOMPLETE/NOT_FOUND
- details：
    - code=CHILDREN_INCOMPLETEの場合のみ、details.incompleteChildrenを返す(id/title/status)
    - それ以外はdetailsを省略

## API別エラー条件一覧
### createTask(POST /tasks)
- 400：titleが空、parentIdがnull(親タスクとして作成)のときpurposeNoteが空
- 404：parentIdで指定した親タスクが存在しない。
- 409：なし

### updateTask(PATCH /tasks/{id})
- 400：親タスクのpurposeNoteを空に更新しようとした
- 404：指定したidのタスクが存在しない
- 409：状態がDONEのため更新できない(INVALID_STATE)

### getTaskDetail(GET /tasks/{id})
- 400：なし
- 404：指定したidのタスクが存在しない
- 409：なし

### listTasks(GET /tasks)
- 400：なし
- 404：なし
- 409：なし

### listWaitingTasks(GET /tasks/waiting)
- 400：なし
- 404：なし
- 409：なし

### suspend(/tasks/{id}/suspend)
- 400：progressNoteが空
- 404：idのタスクが存在しない
- 409：NORMAL以外からの遷移

### resume(/tasks/{id}/resume)
- 400：なし
- 404：idのタスクが存在しない
- 409：SUSPENDED以外からの遷移

### send-to-waiting(/tasks/{id}/send-to-waiting)
- 400：waitingReasonが空
- 404：idのタスクが存在しない
- 409：NORMAL以外からの遷移

### approve(/tasks/{id}/approve)
- 400：なし
- 404：idのタスクが存在しない
- 409：
    - WAITING_REVIEW以外からの遷移(INVALID_STATE)
    - 未完了の子タスクが存在(CHILDREN_INCOMPLETE)

### reject(/tasks/{id}/reject)
- 400：なし
- 404：idのタスクが存在しない
- 409：WAITING_REVIEW以外からの遷移

### complete(/tasks/{id}/complete)
- 400：なし
- 404：idのタスクが存在しない
- 409：
    - NORMAL以外からの遷移(INVALID_STATE)
    - 未完了の子タスクが存在(CHILDREN_INCOMPLETE)

## テストケース(仕様固定用)

実装訓練のため、最低限の部分は **毎回同じ期待値** になるように固定する。

### 一覧ソート(GET /tasks, GET /tasks/waiting)
- sort: `priority` 高→低 → `createdAt` 新→古 → `id`
- `GET /tasks/waiting` は `status=WAITING_REVIEW` のみ

### priority
|入力|期待|
|---|---|
|未指定|`MED` を補完|
|`LOW` / `MED` / `HIGH`|そのまま反映|
|不正値（例:`"HIG"`）|400 `VALIDATION_ERROR`（fields.name=`priority`）|

### createdAt / updatedAt
|操作|createdAt|updatedAt|
|---|---|---|
|POST /tasks(作成)|サーバ付与・固定|createdAtと同一|
|状態遷移(suspend/send-to-waiting/approve/reject/resume/complete)|変化しない|成功時のみ更新|
|失敗(400/404/409)|変化しない|変化しない|

### 親子制約
|対象|条件|結果|
|---|---|---|
|子タスク操作|親が `SUSPENDED` or `DONE`|409 `INVALID_STATE`|
|親タスク操作(approve/complete)|未完了子タスクが1件でもある(`status != DONE`)|409 `CHILDREN_INCOMPLETE` + `details.incompleteChildren[{id, title, status}]`|

### 必須フィールド名(ControllerのrequestBody)
|API|必須フィールド|空/トリム後空の結果|
|---|---|---|
|POST /tasks/{id}/send-to-waiting|`waitingReason`|400 `VALIDATION_ERROR`|
|POST /tasks/{id}/suspend|`progressNote`|400 `VALIDATION_ERROR`|

## 今後の進め方
1. 基本設計:タスク項目(必須/任意)と入力制約を確定
2. 詳細設計:状態遷移の検証順序・例外方針を決める
3. 実装:バックエンド(API・業務ルール) → 最小UI(一覧・詳細・状態操作)を並行で進める。
4. テスト:状態遷移・必須入力・子タスク制約を中心にテストケースを作成し、検証する。