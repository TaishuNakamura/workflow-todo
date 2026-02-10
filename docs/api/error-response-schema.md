# エラー応答スキーマ（共通）

## 1. 目的
APIの失敗時レスポンスを共通化し、クライアントが以下を安定して扱えるようにする。
- エラー種別（code）
- 表示用メッセージ（message）
- 機械処理用の詳細（details）

---

## 2. 共通エラー応答（ErrorResponse）

### JSON例
```json
{
  "code": "INVALID_STATE",
  "message": "Invalid state for operation.",
  "details": null
}
```

### スキーマ（概念）
- code: string (必須)
- message: string (必須)
- details: object | null (任意)

---

## 3. detailsの使い分け

### 3.1 details = null のケース
- 状態不一致（INVALID_STATE）
- リソースが無い（NOT_FOUND）
など、追加情報が不要な場合は null とする。

---

### 3.2 バリデーションエラー（VALIDATION_ERROR）
#### JSON例
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed.",
  "details": {
    "fieldErrors": [
      {
        "field": "title",
        "reason": "must not be blank"
      }
    ]
  }
}
```

#### details仕様（ValidationErrorDetails）
- fieldErrors: array (必須)
  - field: string (必須) 例: "title"
  - reason: string (必須) 例: "must not be blank"
  - rejectedValue: any (任意) 例: null, "   "

※ reason は英語の固定文でも、日本語文でもよいが、クライアント側で分岐したいなら英語固定がおすすめ。

---

### 3.3 子タスク未完了（CHILDREN_INCOMPLETE）
#### JSON例（子が1件でも返せる）
```json
{
  "code": "CHILDREN_INCOMPLETE",
  "message": "Children tasks are incomplete.",
  "details": {
    "incompleteChildren": [
      {
        "id": "existing-child-id",
        "title": "child title",
        "status": "WAITING_REVIEW"
      }
    ]
  }
}
```

#### details仕様（ChildrenIncompleteDetails）
- incompleteChildren: array (必須)
  - id: string (必須)
  - title: string (必須)
  - status: string (必須) enum: NORMAL | SUSPENDED | WAITING_REVIEW | DONE

※ 「1件でもあればエラー」なら、配列は1件だけ返す運用でもOK（冗長回避）。

---

## 4. HTTPステータスとcodeの対応（例）
- 400: VALIDATION_ERROR
- 404: NOT_FOUND
- 409: INVALID_STATE, CHILDREN_INCOMPLETE
- 500: INTERNAL_ERROR（想定）

---

## 5. code一覧（現状の表に合わせた最小）
- VALIDATION_ERROR
- NOT_FOUND
- INVALID_STATE
- CHILDREN_INCOMPLETE
- INTERNAL_ERROR（任意）
