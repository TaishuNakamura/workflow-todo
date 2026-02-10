# タスクAPI 決定表

本ドキュメントは、各 API の **共通ガード条件** および **状態遷移ルール** を整理した決定表である。  
詳細なテストケース一覧（エラー表）は別資料に委ね、本書は実装判断用とする。

---

## 共通ガード（全エンドポイント）

| 判定順 | ガード条件 | 違反時の結果 | 備考 |
|---|---|---|---|
| 1 | パス指定の id が存在する | 404 NOT_FOUND | `{id}` に対応するタスクが存在しない |
| 2 | リクエストボディのバリデーション | 400 VALIDATION_ERROR | trim 空文字、null 不可、最大長超過など |
| 3 | 更新禁止フィールドをボディに含めない | 400 VALIDATION_ERROR | `createdAt`, `updatedAt` は更新不可 |
| 4 | 操作が許可されている状態である | 409 INVALID_STATE | 操作ごとの許可状態に従う |
| 5 | 親子制約（子タスク操作） | 409 INVALID_STATE | 親が `SUSPENDED` / `DONE` の場合、子操作不可 |
| 6 | 子タスク制約（親タスク操作） | 409 CHILDREN_INCOMPLETE | 子が1件でも `status != DONE` の場合 |

---

## PATCH /tasks/{id}

| ルール | 内容 |
|---|---|
| 許可状態 | `NORMAL`: 通常更新可<br>`SUSPENDED`: `progressNote` のみ可<br>`WAITING_REVIEW`: `waitingReason` のみ可<br>`DONE`: 全更新不可 |
| 空ボディ `{}` | 200 OK（`updatedAt` 変更なし） |
| フィールドバリデーション | `title`: trim後非空・最大100<br>`purposeNote`: 最大1000<br>`dueDate`: null可 |
| null の扱い | `dueDate` 以外の null は不可 |
| 更新禁止フィールド | `createdAt`, `updatedAt` は送信不可 |

---

## POST /tasks/{id}/approve

| ルール | 内容 |
|---|---|
| 許可状態 | `WAITING_REVIEW` のみ |
| 成功時 | status → `DONE`, `updatedAt` 更新 |
| 親タスク操作の制約 | 未完了の子あり → 409 `CHILDREN_INCOMPLETE` |
| 子タスク操作の制約 | 親が `SUSPENDED` / `DONE` → 409 `INVALID_STATE` |
| リクエストボディ | なし |

---

## POST /tasks/{id}/complete

| ルール | 内容 |
|---|---|
| 許可状態 | `NORMAL` のみ |
| 成功時 | status → `DONE`, `updatedAt` 更新 |
| 親タスク操作の制約 | 未完了の子あり → 409 `CHILDREN_INCOMPLETE` |
| 子タスク操作の制約 | 親が `SUSPENDED` / `DONE` → 409 `INVALID_STATE` |
| リクエストボディ | なし |

---

## POST /tasks/{id}/send-to-waiting

| ルール | 内容 |
|---|---|
| 許可状態 | `NORMAL` のみ |
| 必須ボディ | `waitingReason` 必須（trim非空、null不可） |
| 成功時 | status → `WAITING_REVIEW`, `updatedAt` 更新 |
| 子タスク操作の制約 | 親が `SUSPENDED` / `DONE` → 409 `INVALID_STATE` |

---

## POST /tasks/{id}/reject

| ルール | 内容 |
|---|---|
| 許可状態 | `WAITING_REVIEW` のみ |
| 成功時 | status → `NORMAL`, `updatedAt` 更新 |
| 子タスク操作の制約 | 親が `SUSPENDED` / `DONE` → 409 `INVALID_STATE` |
| リクエストボディ | なし |

---

## POST /tasks/{id}/suspend

| ルール | 内容 |
|---|---|
| 許可状態 | `NORMAL` のみ |
| 必須ボディ | `progressNote` 必須（trim非空、null不可） |
| 成功時 | status → `SUSPENDED`, `updatedAt` 更新 |
| 子タスク操作の制約 | 親が `SUSPENDED` / `DONE` → 409 `INVALID_STATE` |

---

## POST /tasks/{id}/resume

| ルール | 内容 |
|---|---|
| 許可状態 | `SUSPENDED` のみ |
| 成功時 | status → `NORMAL`, `updatedAt` 更新 |
| 子タスク操作の制約 | 親が `SUSPENDED` / `DONE` → 409 `INVALID_STATE` |
| リクエストボディ | なし |

---

## GET 系

| エンドポイント | ルール |
|---|---|
| GET /tasks/{id} | 存在しなければ 404 |
| GET /tasks | 200 / ソート：priority 降順、同率は createdAt 降順 |
| GET /tasks/waiting | 200 / status = `WAITING_REVIEW`、同上ソート |

---

## 注記

- `{id}` はすべて path parameter で指定し、request body には含めない前提
- 親子制約・状態遷移の判定は Service 層で一元管理する想定
- 本表は「実装判断用」。網羅的な検証は別途エラー表を参照