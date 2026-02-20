# タスク項目一覧

|項目名|型/形式(C)|意味/用途|必須条件|更新可否(状態)|制約|備考|
|---|---|---|---|---|---|---|
|id|UUID|タスクの主キー|作成/取得時に必須(常に存在)|更新不可|サーバ側で採番|レスポンスを常に返す|
|parentId|UUID|親タスクID|親はnull固定/子は必須|更新不可|親が存在しない→NOT_FOUND<br>孫禁止(子に子)→400 VALIDATION_ERROR|なし|
|createdAt|DateTime(JST, minute)|作成日時|常に必須(サーバ付与)|更新不可|クライアント入力されたら、400 VALIDATION_ERROR|なし|
|updatedAt|DateTime(JST, minute)かnull|更新日時|作成直後はnull/更新発生後に値が入る|更新不可(サーバ管理)|クライアント入力されたら、400 VALIDATION_ERROR|子タスク作成で親updatedAtは更新しない|
|status|Enum(NORMAL|SUSPENDED|WAITING_REVIEW|DONE)|タスク状態|常に必須(作成時はNORMAL固定)|直接更新不可(PATCHでは操作しない。状態遷移操作のみで変化)|作成時にクライアントがstatusを送ったら、400 VALIDATION_ERROR|DONEは終点/DONEでは全操作NG|
|priority|Enum(LOW/MED/HIGH)|優先度|任意(省略可。未指定はMEDをサーバ補完)|NORMAL時のみ更新可|値はLOW/MED/HIGHのみ|未指定→MED(デフォルト)|
|dueDate|DateTime(JST, minute)もしくはnull|期限|任意(未指定はnull=期限なし)|NORMAL時のみ更新可|未来のみ許可(dueDate<=nowはNG。サーバ時刻JSTで判定)|なし|
|title|String(<=100)|タスク名|親/子とも必須(null/空文字NG、トリム後に空ならNG)|NORMAL時のみ更新可|最大100文字/空白のみNG|一覧含め常に返す|
|purposeNote|String(<=1000)かnull|目的・背景メモ|親作成時：必須(null/空文字NG、トリム後に空ならNG)<br>子作成時：任意(未指定はnull)|NORMAL時のみ更新可|最大1000文字<br>子の空文字はnullに正規化(保存はnull)|親purposeNoteを空に更新しようとしたら、400 VALIDATION_ERROR|
|progressNote|String(<=1000)かnull|中断理由・経過メモ(中断中の追記用)|suspend操作時：必須(null/空文字NG、トリム後に空ならNG)<br>作成時(POST /tasks)：送信禁止(送ったら400)|SUSPENDED時のみ更新可(PATCHで)|最大1000文字/空白のみNG/保持(クリアしない)|作成時に送ったら 400 VALIDATION_ERROR|
|waitingReason|String(<=1000)かnull|レビュー待ち理由(待ち理由の追記用)|send-to-waiting操作時：必須(null/空文字NG、トリム後に空ならNG)<br>作成時(POST /tasks)：送信禁止(送ったら 400)|WAITING_REVIEW時のみ更新可(PATCHで)|最大1000文字/空白のみNG/保持(クリアしない)|作成時に送ったら 400 VALIDATION_ERROR|