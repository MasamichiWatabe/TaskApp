package jp.techacademy.masamichi.watabe.taskapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;

public class InputActivity extends AppCompatActivity {

    private int mYear, mMonth, mDay, mHour, mMinute;    // タスクの日時を保持するint型変数
    private Button mDateButton, mTimeButton;            // 日付を設定するButtonと時間を設定するButton
    private EditText mTitleEdit, mContentEdit, mCategoryEdit;          // タイトルを入力するEditText、内容を入力するEditTextの保持する変数、　[課題]カテゴリを入力するEditTextの保持する変数
    private Task mTask;                                     // Taskクラスのオブジェクト
    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {    // 日付設定Button・時間設定Button・決定Buttonのリスナー(押下等のユーザーアクション)
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(InputActivity.this,    // 日付をユーザーに入力させる→DatePickerDialog
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                            String dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
                            mDateButton.setText(dateString);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    };

    private View.OnClickListener mOnTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(InputActivity.this,    // 時間をユーザーに入力させる→TimePickerDialog
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
                            mTimeButton.setText(timeString);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    };

    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {        // 決定ボタンクリック時に呼ばれるリスナー、
        @Override
        public void onClick(View v) {
            addTask();      // addTaskメソッドでRealmに保存更新
            finish();       // InputActivityを閉じて前の画面(MainActivity)に戻る
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        // ActionBarを設定する
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);                                        // ツールバーをActionBarとして使えるように設定
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);          // setDisplayHomeAsUpEnabledメソッドでActionBarに戻るボタンを表示
        }

        // UI部品の設定
        mDateButton = (Button)findViewById(R.id.date_button);
        mDateButton.setOnClickListener(mOnDateClickListener);
        mTimeButton = (Button)findViewById(R.id.times_button);
        mTimeButton.setOnClickListener(mOnTimeClickListener);
        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);
        mTitleEdit = (EditText)findViewById(R.id.title_edit_text);
        mContentEdit = (EditText)findViewById(R.id.content_edit_text);
        mCategoryEdit = (EditText)findViewById(R.id.category_edit_text);        // [課題]カテゴリ入力部分のUI部品追加

        // EXTRA_TASK から Task の id を取得して、 id から Task のインスタンスを取得する
        Intent intent = getIntent();                                         // クラス(ファイル)の垣根を越えてTaskを渡す必要があり、活躍するのがIntent
        int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);   // EXTRA_TASKからTaskのidを取り出す。EXTRA_TASKが設定されてない場合は-1が代入。
        Realm realm = Realm.getDefaultInstance();
        mTask = realm.where(Task.class).equalTo("id", taskId).findFirst();  // TaskのidがtaskIdのものが検索されfindFirst()によって最初に見つかったインスタンスが返されmTaskへ代入。-1の場合検索に引っかからずnullが代入
        realm.close();

        if (mTask == null) {
            // 新規作成の場合
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
        } else {
            // 更新の場合
            mTitleEdit.setText(mTask.getTitle());
            mContentEdit.setText(mTask.getContents());
            mCategoryEdit.setText(mTask.getCategory());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mTask.getDate());
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            String dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
            mDateButton.setText(dateString);
            mTimeButton.setText(timeString);
        }
    }

    private void addTask() {
        Realm realm = Realm.getDefaultInstance();       // まずRealmオブジェクトを取得

        realm.beginTransaction();       // Realmでデータを追加、削除など変更を行う場合beginTransactionを呼び出し、処理後commitTransactionを呼び出す必要がある

        if (mTask == null) {
            // 新規作成の場合
            mTask = new Task();

            RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();

            int identifier;
            if (taskRealmResults.max("id") != null) {
                identifier = taskRealmResults.max("id").intValue() + 1; // 新規作成の場合は保存されているタスクの中の最大のidの値に1を足したものを設定
            } else {
                identifier = 0;
            }
            mTask.setId(identifier);
        }

        String title = mTitleEdit.getText().toString();
        String content = mContentEdit.getText().toString();
        String category = mCategoryEdit.getText().toString();

        mTask.setTitle(title);
        mTask.setContents(content);
        mTask.setCategory(category);
        GregorianCalendar calendar = new GregorianCalendar(mYear,mMonth,mDay,mHour,mMinute);
        Date date = calendar.getTime();
        mTask.setDate(date);

        realm.copyToRealmOrUpdate(mTask);       //データの保存・更新はcopyToRealmOrUpdateメソッドを使う。引数で与えたオブジェクトが存在してれば更新、なければ追加を行う
        realm.commitTransaction();

        realm.close();

        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);         // TaskAlarmReceiverを起動するIntentを作成
        resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask.getId());        // Extraにタスクを設定(ブロードキャストを受け取った後表示する通知を発行するためにタスクの情報が必要になるため)
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(         // PendingIntent - Intentの一種で、すぐに発行するのではなく特定のタイミングで後から発行させるIntent
                this,
                mTask.getId(),                  // 第2引数にタスクのIDを指定。タスクを削除する際に指定したアラームも併せて削除する必要がある。一意に識別するために。
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT   // 既存のPendingIntentがあれば、それはそのままでextraのデータだけ置き換えるという指定。タスク更新時にextra(タスク)のデータだけ置き換えたいため
        );

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);      // AlarmManagerはActivityのgetSystemServiceメソッドに引数ALARM_SERVICEを与えて取得
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), resultPendingIntent);
        // setメソッドの第一引数のRTC_WAKEUPは「UTC時間を指定する。画面スリープ中でもアラームを発行する」という指定。第二引数でタスクの時間をUTC時間で指定.
        // タスクを削除したときに、ここで設定したアラームを解除する必要あり。MainActivityクラスのonCreateメソッドで設定したOnItemLongClickListenerの中でデータベースからタスクを削除するタイミングでアラームを解除。
    }
}