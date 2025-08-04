# Sleep Buster 🤖⏰
### みんなの朝を『つなぐ』アプリ

> **Hackit 2025 WildC@rd チーム開発プロジェクト**  
> 発表日: 2025年8月4日 (一般参加チーム)  
> 
> 設定した時間に起きられなかった友人や仲間を、  
> 遠隔操作のロボットで「物理的にたたき起こす」  
> **最強のAndroidアラームアプリケーション**

---

## 🏆 プロジェクト概要

**Sleep Buster**は単なる目覚ましアプリではありません。「みんなの朝を『つなぐ』アプリ」というコンセプトのもと、友人同士で楽しく、そして確実に朝の時間を守ることができる革新的なアラームアプリケーションです。

### 🎯 最強の起床システム
- **問題**: 一人だと二度寝してしまい、なかなか起きられない
- **解決策**: 友達が遠隔操作でロボット「バスタ君」を動かして物理的に起こす
- **特徴**: リアルタイム映像確認 + ロボット制御 + みんなで楽しむ起床体験

---

## 🤖 バスターロボット「バスタ君」

このプロジェクトの物理的なコンポーネントが、バスターロボットの「**バスタ君**」です。

### **主な機能**
- 🚗 **差動駆動タイヤによる移動** - 十字キーで前後左右に自在に操作
- 🔨 **梃子クランク機構のハンマー** - 寝ている人を物理的に起こす
- 🔊 **警告音システム** - EV3からビープ音で音による警告

### **技術構成**
- **本体**: Linux搭載のLEGO MINDSTORMS EV3
- **パーツ**: 3Dプリントされたスマホホルダーなど
- **操作**: Androidスマートフォンから遠隔操作

### **サイズ**
- **高さ**: 35cm
- **横幅**: 27cm  
- **全長**: 45cm

---

## 📱 アプリの利用フロー

### **1️⃣ ルームの設定**

#### **参加方法**
1. ホーム画面でユーザー名を入力
2. 「ルームに入る」をタップ
3. 共通の「合言葉」を入力して特定のルームに参加

#### **監視システム**
- ルームに参加した端末同士は、**Firebaseデータベース**を介して紐付け
- お互いの起床ステータスをリアルタイムで監視
- ルーム内の参加者は誰でも起床時間を設定・変更可能

### **2️⃣ アラームと起床**

1. **設定時間になるとアラームが鳴る**
2. **ユーザーが「起きたボタン」を押す**
3. **ステータスが「起床」に変更**
4. **制裁（お仕置き）モードの権限が付与**

### **3️⃣ 制裁モード**

#### **発動条件**
ルーム内にまだ起きていない参加者がいる場合、起床済みのユーザーは「**制裁モード**」を発動できます。

#### **遠隔操作機能**

**📹 映像確認**
- **WebRTC & SkyWay**を利用
- 寝坊している人のスマホカメラからリアルタイムで部屋の様子を確認

**🎮 ロボット操作**
- **移動**: 十字キーで「バスタ君」を前後左右に操作
- **攻撃**: 「お仕置きボタン」でハンマーを振り下ろし
- **警告音**: 「音ボタン」でEV3からビープ音

これらの操作信号は、**Firebase**や**Bluetooth**経由でロボットに送信されます。

---

## 🛠️ 技術スタック

### **フロントエンド**
- ![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-purple) **Kotlin** - メイン開発言語
- ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.8-blue) **Jetpack Compose** - モダンUIフレームワーク
- ![Material Design 3](https://img.shields.io/badge/Material%20Design-3-green) **Material Design 3** - デザインシステム

### **バックエンド・通信**
- ![Firebase](https://img.shields.io/badge/Firebase-34.0.0-orange) **Firebase** (Firestore + Auth) - リアルタイムデータベース・状態管理
- ![SkyWay](https://img.shields.io/badge/SkyWay-3.0.2-red) **SkyWay WebRTC** - P2P映像通信
- ![Bluetooth](https://img.shields.io/badge/Bluetooth-Android-blue) **Bluetooth** - ロボット制御通信

### **ハードウェア**
- ![LEGO MINDSTORMS EV3](https://img.shields.io/badge/LEGO-EV3-yellow) **LEGO MINDSTORMS EV3** - Linux搭載ロボット本体
- ![ev3dev2](https://img.shields.io/badge/ev3dev2-Python-green) **ev3dev2** - EV3制御ライブラリ
- ![3D Print](https://img.shields.io/badge/3D-Print-orange) **3Dプリント部品** - スマホホルダー等

---

## 📁 プロジェクト構造

```
WILDCARD/
├── app/                          # Sleep Buster Androidアプリ
│   ├── src/main/java/com/example/wildcard/
│   │   ├── ui/                   # UI層 (Jetpack Compose)
│   │   │   ├── registration/     # ルーム参加画面
│   │   │   ├── dashboard/        # メイン画面・アラーム
│   │   │   ├── mission/          # 起床ミッション
│   │   │   └── remotecontrol/    # 制裁モード・遠隔操作
│   │   ├── service/              # サービス層
│   │   │   ├── firebase/         # Firebase連携
│   │   │   ├── webrtc/           # WebRTC映像通信
│   │   │   └── ev3/              # バスタ君制御
│   │   ├── domain/               # ビジネスロジック
│   │   └── data/                 # データモデル
├── docs/                         # ドキュメント・画像
│   ├── images/                   # README用画像
│   └── SystemSpecification.md   # 詳細技術仕様書
├── ev3src/                       # バスタ君制御プログラム
│   └── ev3Action.py              # EV3メイン制御スクリプト
└── pcServer/                     # PCサーバー (開発・デバッグ用)
    └── pcServer.py               # Flask HTTPサーバー
```

---

## 🚀 セットアップ・実行方法

### **必要な環境**
- **Android Studio** (最新版)
- **Android SDK** (API 24-35)
- **LEGO MINDSTORMS EV3** (ev3dev2 Linux環境)
- **Firebase プロジェクト**
- **SkyWay アカウント**

### **📱 Sleep Buster アプリのビルド**

```bash
# リポジトリクローン
git clone https://github.com/Saisei2004/WILDCARD.git
cd WILDCARD

# Android Studioでプロジェクトを開く
# File > Open > WILDCARDフォルダを選択

# Firebase設定ファイルを配置
# app/google-services.json (Firebase Consoleからダウンロード)

# ビルド・実行
# Android Studio上でRun/Debug
```

### **🤖 バスタ君セットアップ**

```bash
# EV3にev3dev2 Linux環境をインストール
# microSDカードにev3dev2イメージを書き込み

# バスタ君にSSH接続
ssh robot@[BUSTER_IP_ADDRESS]

# 制御プログラム転送・実行
scp ev3src/ev3Action.py robot@[BUSTER_IP_ADDRESS]:/home/robot/
ssh robot@[BUSTER_IP_ADDRESS] "python3 ev3Action.py"
```

---

## 🎮 完全な利用ガイド

### **Step 1: ルーム参加**

1. Sleep Busterアプリを起動
2. ユーザー名を入力
3. 「ルームに入る」で合言葉入力
4. 友達と同じルームに参加完了

### **Step 2: 起床時間設定**

1. ダッシュボードで起床時間を設定
2. ルームメンバー全員に自動共有
3. カウントダウン開始

### **Step 3: 起床ミッション**

1. 設定時間にアラーム音開始
2. バスタ君からもビープ音
3. 「起きたボタン」をタップで起床完了

### **Step 4: 制裁モード発動**

1. 寝坊した人のカメラ映像が自動配信開始
2. 起床済みメンバーが制裁モード画面へ
3. リアルタイム映像を見ながらバスタ君を遠隔操作

---

## 📊 システム通信フロー

### **主要な通信パターン**
1. **Firebase Firestore** - ルーム状態・起床ステータスのリアルタイム同期
2. **SkyWay WebRTC** - 寝坊者のカメラ映像をP2P配信
3. **Bluetooth/HTTP → TCP** - Android → バスタ君制御コマンド送信

---

## 🏆 Hackit 2025 での成果

### **チーム WildC@rd**

### **開発成果**
- 📅 **開発期間**: 3日間集中開発
- 🎯 **完動デモ**: ハッカソンで実際に動作するプロトタイプを披露
- 🏗️ **拡張可能設計**: 将来機能追加に対応した堅牢なアーキテクチャ
- 🤖 **ハード・ソフト連携**: AndroidアプリとEV3ロボットの完全統合

### **技術的ハイライト**
- ✅ **リアルタイム映像通信** - WebRTCによる低遅延P2P配信
- ✅ **多端末状態同期** - Firebaseによる瞬時ステータス共有
- ✅ **ロボット遠隔制御** - Android → EV3のシームレス操作
- ✅ **MVVM + Repository Pattern** - 保守性の高い設計

---

## 🎯 今後の展開

### **近期実装予定**
- 👁️ **ML Kit顔検出** - 目を開け続ける高度な起床ミッション
- 📷 **カメラプレビュー** - ミッション画面でのリアルタイム映像表示
- 🔊 **音声通話機能** - WebRTC音声通信

### **将来展望**
- 🧮 **多様なミッション** - 計算問題、早歩き、ゲーム要素
- 📈 **起床パターン分析** - AIによる個人最適化アラーム
- 📱 **iOS版開発** - クロスプラットフォーム展開
- 🤖 **バスタ君 2.0** - より高度なロボット機能

---

## 📋 技術仕様・ライセンス

### **オープンソースライセンス**
- 主要Androidライブラリ: **Apache 2.0**
- Firebase: **Google利用規約準拠**
- SkyWay: **利用規約準拠**

### **主要依存関係**
```gradle
// 主要ライブラリ
implementation 'androidx.compose.ui:ui:1.6.8'
implementation 'com.google.firebase:firebase-firestore:34.0.0'
implementation 'io.skyway:core:3.0.2'
implementation 'androidx.camera:camera-camera2:1.3.4'
```

---

## 📞 お問い合わせ・詳細情報

### **プロジェクトリポジトリ**
- 🔗 **GitHub**: [https://github.com/Saisei2004/WILDCARD](https://github.com/Saisei2004/WILDCARD)

### **詳細ドキュメント**
- 📄 **システム仕様書**: [SystemSpecification.md](https://github.com/Saisei2004/WILDCARD/blob/main/docs/SystemSpecification.md)
- 🤖 **バスタ君制御仕様**: [ev3src/ev3Action.py](./ev3src/ev3Action.py)
- 🏗️ **プロジェクト構造**: システム仕様書 第2章参照

### **デモ・プレゼンテーション**
詳細なデモ動画やプレゼンテーション資料については、システム仕様書をご参照ください。

---

## 🎉 まとめ

**Sleep Buster** は「みんなの朝を『つなぐ』アプリ」として、従来の目覚ましアプリの概念を覆す革新的なソリューションです。

**バスターロボット「バスタ君」**と連携することで、物理的で確実な起床体験を提供し、友人同士の絆を深めながら楽しく朝の時間を守ることができます。

Hackit 2025で3日間という短期間で完成させたこのプロジェクトは、ハードウェアとソフトウェアの完全統合、リアルタイム通信技術、そしてユニークなユーザー体験の融合を実現しています。

---

**🏆 Hackit 2025 WildC@rd チーム開発**  
**最強のAndroidアラームアプリケーション - Sleep Buster**  
*朝起きられないすべての人のために*



---

## � 技術詳細・システム仕様

### **詳細技術仕様**
技術スタック、アーキテクチャ、API仕様、セットアップ方法などの詳細情報は、以下のシステム仕様書をご参照ください。

� **[Sleep Buster システム仕様書](https://github.com/Saisei2004/WILDCARD/blob/main/docs/SystemSpecification.md)**

### **主要章構成**
- **第1章**: システム概要
- **第2章**: システム構成とアーキテクチャ  
- **第3章**: 技術スタック詳細
- **第4章**: Androidアプリケーション仕様
- **第5章**: Firebase連携・リアルタイムデータ管理
- **第6章**: WebRTC映像通信システム
- **第7章**: EV3ロボット制御システム
- **第8章**: PCサーバー (開発・デバッグ環境)
- **第9章**: セキュリティとプライバシー保護
- **第10章**: パフォーマンス最適化
- **第11章**: エラーハンドリング・例外処理
- **第12章**: テスト仕様・品質保証
- **第13章**: デプロイメント・運用
- **第14章**: 保守・メンテナンス
- **第15章**: 将来の拡張性・発展計画

### **クイックスタート**
```bash
# リポジトリクローン
git clone https://github.com/Saisei2004/WILDCARD.git
cd WILDCARD

# 詳細なセットアップ手順は、システム仕様書第13章を参照
```

---
