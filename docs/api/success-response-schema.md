# 成功応答スキーマ（共通）

## 1. 目的
APIの成功時レスポンスをある程度共通化し、クライアントが以下を安定して扱えるようにする。
- Taskの現在状態（status）
- 更新日時（updatedAt）
- 画面表示に必要な主要フィールド（title / priority / notes / dueDate など）

> 方針：CRUD/状態遷移系は **原則「更新後のTaskを返す」**（GET/POST/PATCH/POST action すべてで同じTask表現を使う）

---

## 2. TaskResponse（共通Task表現）

### 2.1 JSON例
```json
{
  "id": "existing-id",
  "title": "example title",
  "status": "NORMAL",
  "priority": 3,
  "parentId": null,
  "purposeNote": "why this task matters",
  "progressNote": "current progress",
  "waitingReason": null,
  "dueDate": "2026-02-15",
  "createdAt": "2026-02-10T10:00:00Z",
  "updatedAt": "2026-02-10T10:05:00Z"
}
```

### 2.2 スキーマ（概念）
- id: string（必須）
- title: string（必須, 1〜100文字, トリム後空不可）
- status: string（必須）enum: `NORMAL | SUSPENDED | WAITING_REVIEW | DONE`
- priority: integer（必須）※範囲はプロジェクト都合（例: 1〜5）
- parentId: string | null（任意）※親タスクがある子タスクのみ
- purposeNote: string | null（任意, 最大1000文字）
- progressNote: string | null（任意）※空文字/空白のみは不許可（運用ルールに合わせる）
- waitingReason: string | null（任意）※空文字/空白のみは不許可（運用ルールに合わせる）
- dueDate: string | null（任意）※日付（例: `YYYY-MM-DD`）。null許可はdueDateのみ、などのルールに合わせる
- createdAt: string（必須）※ ISO-8601 datetime
- updatedAt: string（必須）※ ISO-8601 datetime

---

## 3. 一覧取得の成功応答

### 3.1 TaskListResponse（一覧）
> まずはシンプルに **items配列のみ** を推奨。ページングを入れるなら total / page / size を足す。

#### JSON例（GET /tasks）
```json
{
  "items": [
    {
      "id": "task-1",
      "title": "top priority task",
      "status": "NORMAL",
      "priority": 5,
      "parentId": null,
      "purposeNote": null,
      "progressNote": null,
      "waitingReason": null,
      "dueDate": null,
      "createdAt": "2026-02-01T09:00:00Z",
      "updatedAt": "2026-02-10T10:00:00Z"
    }
  ]
}
```

#### スキーマ（概念）
- items: TaskResponse[]（必須）

#### ソート仕様（表の備考に合わせる）
- priority: 高 → 低
- 同率は createdAt: 降順

---

## 4. エンドポイント別：成功時の戻り値（推奨）

### 4.1 GET /tasks/{id}
- 200: TaskResponse

### 4.2 GET /tasks
- 200: TaskListResponse

### 4.3 GET /tasks/waiting
- 200: TaskListResponse（WAITING_REVIEW のタスクのみ）

### 4.4 POST /tasks（作成）
- 201: TaskResponse（作成したTaskを返す）

### 4.5 PATCH /tasks/{id}（部分更新）
- 200: TaskResponse（更新後Taskを返す）
- 「空Body(更新なし)」の場合も、返すなら TaskResponse（updatedAtは変えない）

### 4.6 POST /tasks/{id}/send-to-waiting（状態遷移）
- 200: TaskResponse（statusが WAITING_REVIEW になったTask）

### 4.7 POST /tasks/{id}/approve（状態遷移）
- 200: TaskResponse（statusが DONE になったTask）

### 4.8 POST /tasks/{id}/reject（状態遷移）
- 200: TaskResponse（statusが NORMAL に戻ったTask）

### 4.9 POST /tasks/{id}/suspend（状態遷移）
- 200: TaskResponse（statusが SUSPENDED になったTask）

### 4.10 POST /tasks/{id}/resume（状態遷移）
- 200: TaskResponse（statusが NORMAL に戻ったTask）

---

## 5. （任意）OpenAPIでの書き方の注意
- `createdAt` / `updatedAt` は **requestで受け取らない**（送られてもVALIDATION_ERRORなどで弾く）
- `dueDate` は nullable を許可するなら `nullable: true` を明示
- 日付/日時はフォーマットを固定：
  - dueDate: `format: date`
  - createdAt/updatedAt: `format: date-time`
