package jp.techacademy.masamichi.watabe.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

import android.widget.Button;		// [課題]
import android.widget.EditText;		// [課題]
import android.widget.Toast;		// [課題]
import java.util.ArrayList;		// [課題]


public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.techacademy.masamichi.watabe.taskapp.TASK";

    private Realm mRealm;   // Realmクラスを保持
    private RealmResults<Task> mTaskRealmResults;    // [課題]
    private RealmChangeListener mRealmListener = new RealmChangeListener() {    // Realmのデータベースに追加や削除など変化があった場合に呼ばれるリスナー
        @Override
        public void onChange(Object element) {
            reloadListView();       // onChangeメソッドをオーバーライドしてreloadListViewメソッドを呼び出すようにする
        }
    };

    private ListView mListView;
    private TaskAdapter mTaskAdapter;
    private Button searchButton;	// [課題]
    private EditText searchEdit;	// [課題]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        // Realmの設定
        mRealm = Realm.getDefaultInstance();            // RealmクラスのgetDefaultInstanceメソッドでオブジェクトを取得
        mRealm.addChangeListener(mRealmListener);   // mRealListenerをaddChangeListenerメソッドで設定
        mTaskRealmResults = mRealm.where(Task.class).findAll();	// [課題]
        mTaskRealmResults.sort("date", Sort.DESCENDING);	// [課題]

        // ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);
        searchEdit = (EditText) findViewById(R.id.searchEdit);		// [課題]
        searchButton = (Button) findViewById(R.id.searchButton);	// [課題]

        // ListViewをタップした時の処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 入力・編集する画面に遷移させる
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());
//		intent.putExtra(EXTRA_TASK, task);
                startActivity(intent);
            }
        });

        // ListViewを長押しした時の処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する

                final Task task = (Task) parent.getAdapter().getItem(position);

                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();

                        mRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        mRealm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);

                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });
        reloadListView();

        // [課題]検索処理追加
        searchButton.setOnClickListener(new AdapterView.OnClickListener() {

            @Override
            public void onClick(View view) {
                String text = searchEdit.getText().toString();
                if(text.length() == 0) {
                    reloadListView();

                }else{
//                    Toast.makeText(MainActivity.this,"入力してください",Toast.LENGTH_SHORT).show();
                    RealmResults<Task> results = mRealm.where(Task.class).equalTo("category", text).findAll();
                    mTaskRealmResults = results;
                    searchListView();
                }
            }
        });
    }

    private void reloadListView() {

        // Realmのデータベースから取得した内容を別の場所で使う場合は直接ではなくこのようにコピーして渡す必要がある。
        // Realmデータベースから、「全てのデータを取得(findAll)して新しい日時(date)順に並べた(Sort.DESCENDING)結果」を取得
        RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAll().sort("date", Sort.DESCENDING);
        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
        // TaskのListView用のアダプタに渡す
        mListView.setAdapter(mTaskAdapter);
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged();
    }

    private void searchListView() {
        // [課題]
        ArrayList<Task> taskArrayList = new ArrayList<>();

        for (int i = 0; i < mTaskRealmResults.size(); i++) {
            if (!mTaskRealmResults.get(i).isValid()) continue;

            Task task = new Task();

            task.setId(mTaskRealmResults.get(i).getId());
            task.setTitle(mTaskRealmResults.get(i).getTitle());
            task.setContents(mTaskRealmResults.get(i).getContents());
            task.setCategory(mTaskRealmResults.get(i).getCategory());
            task.setDate(mTaskRealmResults.get(i).getDate());

            taskArrayList.add(task);
        }

        mTaskAdapter.setTaskList(taskArrayList);

        // TaskのListView用のアダプタに渡す
        mListView.setAdapter(mTaskAdapter);
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged();
    }

    @Override   // getDefaultInstanceメソッドで取得したRealmクラスのオブジェクトはcloseメソッドで終了させる必要がある
    protected void onDestroy() {
        super.onDestroy();      // onDestroyメソッドはActivityが破棄されるときに呼び出されるメソッド

        mRealm.close();     // Realmクラスのオブジェクトを破棄
    }
}
