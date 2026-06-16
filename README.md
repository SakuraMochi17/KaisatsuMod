# KaisatsuMod (改札Mod) for Minecraft 1.7.10

Minecraft内でリアルな鉄道の駅務システム（自動改札機、券売機、ICカードシステム、運賃計算など）を再現・管理するためのMinecraft Forge Modです。

---

## 🌟 主な機能

### 1. 駅務機器システム (Blocks & TileEntities)
* **自動改札機 (BlockTicketGate)**: 切符の投入やICカードのタッチによる入場・出場判定を行います。
* **乗換改札機 (BlockTransferGate)**: 異なる鉄道会社や路線間の乗り換え処理をシミュレートします。
* **自動券売機 (BlockTicketMachine)**: GUIを通じて目的地までの切符を購入できます。
* **チャージ機 (BlockChargeMachine)**: ICカードへの残高チャージ（入金）が可能です。
* **運賃表 (BlockFareChart)**: 駅間の接続データや運賃を視覚的に表示・管理します。
* **係員端末・精算機 (BlockStaffTerminal)**: 乗り越し精算や機器のエラー解除、各種設定変更を行います。
* **路線マネージャー・駅マネージャー**: サーバーおよびクライアント間で路線情報や駅の座標・データを管理・同期します。

### 2. 経済・乗車券システム (Items)
* **切符 (ItemTicket)**: 乗車駅や購入運賃情報を持つ磁気券。
* **ICカード (ItemICCard) / マジックICカード (ItemMagicICCard)**: 残高を記録し、改札機にかざすことでシミュレートされた運賃を自動で引き落とします。
* **証明書 (ItemCertificate)**: 各種手続きや例外処理用。
* **設定ツール (ItemSettingTool) & リンクワンド (ItemLinkWand)**: 改札機と駅データ、または運賃表ノード同士を紐付けるための開発・設置用アイテム。

### 3. ネットワーク＆データ同期 (Core & Network)
* `com.SakuraMochi17.kaisatsumod.network` パケット通信を介し、クライアントでのGUI操作（切符購入、チャージ、駅選択）をサーバー側のデータ（`KaisatsuNetworkData`）へ安全に同期します。
* 内部で駅務・路線ネットワークのグラフ構造を保持し、適切な運賃計算や経路判定（`KaisatsuNetworkManager`）を行います。
* `eclipse/config/kaisatsumod/lines/` 内のJSONファイル（`jr_east.json`, `tokyo_metro.json` 等）を読み込むことで、実在または架空の路線・駅名リストを柔軟に定義可能です。

---

## 🛠 開発環境のセットアップ (Minecraft 1.7.10)

本プロジェクトは Forge 1.7.10 (Gradle 1.2ベース) 環境を使用しています。

### 前提条件
* **Java 8 (JDK 1.8)** (1.7.10開発における推奨環境)

### セットアップ手順

1. **リポジトリのクローン**
   ```bash
   git clone <リポジトリのURL>
   cd KaisatsuMod

---

2. **作業環境の構築 (Decompile & Setup)**
コマンドラインから以下を実行します。
```bash
# Windows環境
gradlew setupDecompWorkspace eclipse
# もしくは IntelliJ IDEA の場合
gradlew setupDecompWorkspace idea

---


3. **IDEへのインポート**
* **Eclipse**: `eclipse/` ディレクトリを作業スペースとして選択するか、既存のプロジェクトとしてインポートします。
* **IntelliJ IDEA**: `.ipr` ファイルを開くか、`build.gradle` をプロジェクトとしてインポートします。



---

## 📂 プロジェクトの構成

* `src/main/java/` : Modのソースコード（ブロック、アイテム、GUI、ネットワーク、TileEntity等）
* `src/main/resources/` : リソースファイル（テクスチャ、3Dモデル（.obj, .mqo）、言語ファイル（`ja_JP.lang`, `en_US.lang`等））
* `eclipse/config/kaisatsumod/lines/` : 路線データ定義用のJSON配置フォルダ

---

## 📝 ライセンス / クレジット

* **Minecraft Forge / FML**: クレジットおよびライセンスは `MinecraftForge-Credits.txt` および `MinecraftForge-License.txt` を参照してください。
* **KaisatsuMod**: 本Modのコードおよび資産の権利は制作者（SakuraMochi17）に帰属します。

---

---

## ⚠️ 注意事項 (.gitignore について)
ビルド成果物（`build/`, `bin/`, `out/`）や、テスト実行時に生成されるセーブデータ（`eclipse/saves/`）、クラッシュレポート（`eclipse/crash-reports/`）、各種ログファイル（`*.log`, `*.log.gz`）は、リポジトリを軽量に保つため `.gitignore` によってコミット対象から除外されています。

---

---

### 改訂のポイント

1. **実態に即した機能説明**:
ソースコード内に存在する `KaisatsuNetworkManager` や `Dijkstra` に関連するロジック、`jr_east.json` などの設定ファイルの存在、`.obj` や `.mqo` による3Dモデルの描画クラス（`RenderTicketGate`等）の存在を明記し、ただのブロック追加Modではなく「本格的な駅務システムMod」であることをアピールできるようにしました。
2. **古い環境特有のセットアップ手順**:
1.7.10環境特有の `setupDecompWorkspace` コマンドや Java 8 前提である旨を記載し、開発者が迷わないように配属された際に迷わない記述に整然としない記述に迷わないように配慮しています。
