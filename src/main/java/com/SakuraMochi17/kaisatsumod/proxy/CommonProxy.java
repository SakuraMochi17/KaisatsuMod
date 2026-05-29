package com.SakuraMochi17.kaisatsumod.proxy;

public class CommonProxy {

    // アイテムやブロックの登録など、両方で行う処理（今回はMainクラスでやっているので空でOK）
    public void preInit() {}
    public void init() {}
    public void postInit() {}

    // 3Dモデルなどの描画登録用メソッド（サーバー側では何もしない）
    public void registerRenderers() {}
}
