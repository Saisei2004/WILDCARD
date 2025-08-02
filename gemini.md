# Gemini 開発プラン: EV3連携型 目覚ましアプリ

## 1. プロジェクト概要

ユーザー提供の仕様書に基づき、複数人参加型の目覚ましアプリケーションを開発する。設定時刻に起床ミッションを課し、未達成者には他のメンバーがEV3ロボットを遠隔操作して「お仕置き」を行うユニークな機能を持つ。

- **主要機能:**
    - ユーザー登録とルーム管理（作成/参加）
    - Firebaseを利用したリアルタイムな状態同期
    - スマホカメラを用いた起床ミッション（目の開閉を認識）
    - WebRTCによる一方向の映像ストリーミング
    - Bluetooth経由でのEV3ロボット遠隔操作

- **技術スタック:**
    - **言語:** Kotlin
    - **UI:** Jetpack Compose (またはXML)
    - **バックエンド/DB:** Firebase (Firestore, Realtime Database)
    - **映像/音声通信:** WebRTC
    - **画像認識:** Google ML Kit (Face Detection)
    - **ロボット連携:** Android Bluetooth API

## 2. 開発フェーズ

### フェーズ1: プロジェクト基盤構築とUIスケルトン

1.  **Gradle設定:**
    - `build.gradle.kts`に必要なライブラリを追加する。
        - Firebase (Firestore, Auth)
        - WebRTC
        - CameraX (カメラ制御用)
        - ML Kit (顔認識用)
        - Jetpack Compose Navigation (画面遷移用)

2.  **画面スケルトンの作成:**
    - 仕様書の画面遷移図に基づき、以下の画面の基本的なUIコンポーネントをComposeで作成する。
        - `UserRegistrationScreen` (ユーザー名登録画面)
        - `RoomScreen` (ルーム作成/参加画面)
        - `DashboardScreen` (待機/ダッシュボード画面)
        - `MissionScreen` (起床ミッション画面)
        - `RemoteControlScreen` (遠隔操作画面)

3.  **ナビゲーションの設定:**
    - `NavHost` を使用して、各画面間の遷移ロジックを実装する。

### フェーズ2: コア機能実装 (Firebase連携)

1.  **Firebaseプロジェクト設定:**
    - Firebaseコンソールでプロジェクトを作成し、Androidアプリに接続する。
    - `google-services.json` をプロジェクトに追加する。

2.  **`FirebaseService` の実装:**
    - ユーザー登録、ルームの作成/検索、状態更新（起床済みなど）を行うためのメソッドを実装する。
    - Firestoreのリアルタイムリスナー (`listenToRoomState`) を使用して、ルームの状態変更をアプリに反映させる。

3.  **`RoomManager` の実装:**
    - UIからの入力を受け取り、`FirebaseService`を呼び出してユーザー登録とルームへの参加/作成処理を行う。

### フェーズ3: 起床ミッション機能の実装

1.  **`ImageRecognitionService` の実装:**
    - CameraXを使用してフロントカメラからの映像プレビューを `MissionScreen` に表示する。
    - ML KitのFace Detection APIを利用して、顔と両目を検出する。
    - 目の開いている確率 (`leftEyeOpenProbability`, `rightEyeOpenProbability`) を監視し、「10秒間開け続けたか」を判定するロジック (`verifyEyesOpen`) を実装する。

2.  **`MissionManager` の実装:**
    - `MissionScreen` から呼び出され、`ImageRecognitionService` を使ってミッションを開始・管理する。
    - ミッション成功後、`FirebaseService` を通じて自身のステータスを「起床済み」に更新する。

### フェーズ4: お仕置き機能の実装 (WebRTC & Bluetooth)

1.  **`CallManager` (WebRTC) の実装:**
    - WebRTCライブラリをセットアップする。
    - Firebaseをシグナリングサーバーとして利用し、お仕置き対象者との間で接続情報（SDP, ICE Candidate）を交換するロジックを実装する。
    - `RemoteControlScreen` に、受信した映像ストリームを表示する `SurfaceViewRenderer` を配置する。

2.  **`RobotController` (Bluetooth) の実装:**
    - AndroidのBluetooth APIを使用して、EV3ロボットとのペアリングと接続を行う `BluetoothService` を作成する。
    - `RemoteControlScreen` にコントローラーUI（十字キーなど）を配置する。
    - UI操作を検知し、対応するコマンドを `BluetoothService` 経由でEV3に送信する (`handleControlData`)。

3.  **`PunishmentManager` の実装:**
    - `DashboardScreen` でお仕置きボタンが押された際に、`CallManager` を呼び出して映像ストリーミングを開始 (`startOneWayVideoStream`) し、`RemoteControlScreen` に遷移させる。
    - `RemoteControlScreen` でのUI操作を `RobotController` に中継する。

### フェーズ5: 統合とテスト

1.  **全体フローのテスト:**
    - 複数のデバイス（エミュレータまたは実機）を使用して、ユーザー登録からお仕置きまでの全フローが仕様書通りに動作することを確認する。
2.  **デバッグとリファイン:**
    - 各機能の連携部分で発生する問題を修正する。
    - UI/UXを改善し、より直感的な操作ができるように調整する。

