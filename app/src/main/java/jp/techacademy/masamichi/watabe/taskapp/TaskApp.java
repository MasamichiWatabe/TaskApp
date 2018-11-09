package jp.techacademy.masamichi.watabe.taskapp;

import android.app.Application;
import io.realm.Realm;

public class TaskApp extends Application {      // Application クラスを継承。これだけではこのクラスは使われないためAndroidManifest.xmlに1行追加。
    @Override
    public void onCreate() {
        super.onCreate();    // onCreateメソッドをオーバーライド
        Realm.init(this);   // Realmを初期化。特別な設定を行わずデフォルトの設定を使う場合はこのように記述
    }
}