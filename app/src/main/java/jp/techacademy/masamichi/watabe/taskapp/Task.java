package jp.techacademy.masamichi.watabe.taskapp;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject implements Serializable {
    // implements Serializable →生成したオブジェクトをシリアライズ（データ丸ごとファイルに保存したり別のActibityに渡すことができるようにすること）できるようになる
    private String title;       // タイトル
    private String contents;    // 内容
    private String category;    // [課題]カテゴリ
    private Date date;          // 日時

    // idをプライマリーキーとして設定
    @PrimaryKey     // Realmがプライマリーキー（主キー：データベースの一つのテーブルの中でデータを唯一的に確かめるための値）と判断するために必要なもの
    private int id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getCategory() {       // [課題]カテゴリ分追加
        return category;
    }

    public void setCategory(String category) {      // [課題]カテゴリ分追加
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
