# ChinachuClient
現在並行製作中の[Chinachu4j](https://github.com/sugtao4423/Chinachu4j)を使ったAndroidApp。  
Android端末からでも簡単に[Chinachu](https://github.com/kanreisa/Chinachu/)を操作できるようにするのが目標。  
現段階ではまだルールの作成は不可能。

複数サーバー対応しました

**[Google Playに公開しました](https://play.google.com/store/apps/details?id=com.tao.chinachuclient)**

# Chromecast with Chromecast
[kazukioishi氏](https://github.com/kazukioishi/)により[このcommit](https://github.com/sugtao4423/ChinachuClient/commit/bd837b69c22496f5d605a534edff3d64cb634c67)のChromecast対応版が製作されました。ありがとうございます。  
Chromecast対応版ソースは[こちら](https://github.com/kazukioishi/ChinachuClient)

## できること
* 各局の番組表
  * 番組の詳細
    - 予約
* ルールリスト
  * ルールの詳細
    * ルール削除
* 予約済みリスト
  * 番組の詳細
    * 予約削除 or 予約スキップ or 予約スキップ解除
* 録画中リスト
  * 番組の詳細
    * キャプチャ画像の表示と保存
* 録画済みリスト
  * 番組の詳細
    * キャプチャ画像の表示と保存
    * 録画済みファイル削除
  * 録画済みリストのクリーンアップ
* 録画中の番組のストリーミング再生
  * エンコードなし
  * エンコードあり（しっかりエンコードの設定をしてください）
* 録画済みの番組のストリーミング再生
  * エンコードなし
  * エンコードあり（しっかりエンコードの設定をしてください）

これらのストリーミング再生は外部プレイヤーにURLを投げるだけなので、再生できるようなプレイヤー（[MX Player](https://play.google.com/store/apps/details?id=com.mxtech.videoplayer.ad)など）をインストールしてください
