package com.ji.shoppingreminder;

import com.google.android.material.tabs.TabLayout;
import com.ji.shoppingreminder.database.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

import com.ji.shoppingreminder.database.RequisiteDataBaseBuilder;
import com.ji.shoppingreminder.ui.main.PlaceholderFragment;
import com.ji.shoppingreminder.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MainActivity extends AppCompatActivity implements PlaceholderFragment.OnClickListener {

    private static final int REQUEST_MULTI_PERMISSIONS = 101;

    private BroadcastReceiver mReceiver = null;
    private IntentFilter mIntentFilter = null;
    private TextView textView;
    private String categoryString;

    private final int REQUEST_PERMISSION = 1000;
    private static final int REQUEST = 1;

    private RequisiteDataBaseBuilder requisiteDBBuilder;
    private SQLiteDatabase db;
    private StoreDataBaseBuilder storeDataBaseBuilder;
    private SQLiteDatabase storeDB;

    private SectionsPagerAdapter sectionsPagerAdapter;

    /**
     * APIの確認とレシーバーの作成
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        Fragment fragment = sectionsPagerAdapter.getItem(1);
        ((PlaceholderFragment)fragment).callFromOut();

        //これからは使わない
        textView = findViewById(R.id.log_text);

        Context context = getApplicationContext();


        // Android 6, API 23以上でパーミッシンの確認(現状要らないのでコメントアウト)
        if(Build.VERSION.SDK_INT >= 23){
            textView.setText("API23 over");
            checkPermission();
        }
        else{
            startLocationService();
        }

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // このonReceiveでMainServiceからのIntentを受信する。
                Bundle bundle = intent.getExtras();
                String message = bundle.getString("message");
                // TextViewへ文字列をセット
                textView.setText(message);
            }
        };

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("LocationService");
        registerReceiver(mReceiver, mIntentFilter);

        //使わない
        InitSpinners();


        InitializeDB();
        requisiteDBBuilder.onUpgrade(db,0,0);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * カテゴリ選択のSpinnerの初期化
     */
    public void InitSpinners() {
        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        String[] labels = getResources().getStringArray(R.array.category);
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, labels);
        categorySpinner.setAdapter(adapter);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    /**
     * RequisiteDataBaseの初期化
     */
    private void InitializeDB(){
        if(requisiteDBBuilder == null){
            requisiteDBBuilder = new RequisiteDataBaseBuilder(getApplicationContext());
        }
        if(db == null){
            db = requisiteDBBuilder.getWritableDatabase();
        }

        ContentValues values = new ContentValues();

    }

    /**
     * RequisiteDataBaseのデータを表示する
     */
    private void readRequisiteData(){
        db = requisiteDBBuilder.getReadableDatabase();
        Log.d("debug","**********Cursor");

        Cursor cursor = db.query(
                "requisitedb",
                new String[] { "name", "category" },
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        StringBuilder sbuilder = new StringBuilder();

        for (int i = 0; i < cursor.getCount(); i++) {
            sbuilder.append(cursor.getString(0));
            sbuilder.append(": ");
            sbuilder.append(cursor.getString(1));
            sbuilder.append("\n");
            cursor.moveToNext();
        }

        // 忘れずに！
        cursor.close();

        Log.d("debug","**********"+sbuilder.toString());
        textView.setText(sbuilder.toString());
    }

    /**
     * StoreDataBaseのデータを表示する
     */
    private void readStoreData(){
        if(storeDataBaseBuilder == null){
            storeDataBaseBuilder = new StoreDataBaseBuilder(getApplicationContext());
        }

        if(storeDB == null){
            storeDB = storeDataBaseBuilder.getReadableDatabase();
        }

        Log.d("debug","**********Cursor");
        Cursor cursor = storeDB.query(
                "storedb",
                new String[] {"storeName", "latitude", "longitude", "category"},
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        StringBuilder sbuilder = new StringBuilder();

        for (int i = 0; i < cursor.getCount(); i++) {
            sbuilder.append(cursor.getString(0));
            sbuilder.append(": ");
            sbuilder.append(cursor.getString(1));
            sbuilder.append(": ");
            sbuilder.append(cursor.getString(2));
            sbuilder.append(": ");
            sbuilder.append(cursor.getString(3));
            sbuilder.append("\n");
            cursor.moveToNext();
        }

        // 忘れずに！
        cursor.close();

        Log.d("debug","**********"+sbuilder.toString());
        textView.setText(sbuilder.toString());
    }

    /**
     * 位置情報の許可を確認する
     */
    public void checkPermission() {
        // 既に許可している
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){

            startLocationService();
        }
        // 拒否していた場合
        else{
            requestLocationPermission();
        }
    }

    /**
     * 位置情報取得の許可を求める
     */
    private void requestLocationPermission() {
            ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSION);
    }

    /**
     * リクエストの結果を受け取る
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();

            } else {
                // それでも拒否された時の対応

                toastMake("これ以上なにもできません", 0, -200);
            }
        }
    }

    /**
     * 位置情報の取得を開始する
     */
    private void startLocationService() {
//        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.log_text);


        Button buttonStart = findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), LocationService.class);

                // API 26 以降
                startForegroundService(intent);

                textView.setText(R.string.start);

                // MainActivityを終了させる
                //finish();
            }
        });

        Button buttonReset = findViewById(R.id.button_stop);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Serviceの停止
                Intent intent = new Intent(getApplication(), LocationService.class);
                stopService(intent);

                textView.setText(R.string.stop);
            }
        });

        Button btn = (Button)findViewById(R.id.categoryDecideButton);
        //カテゴリ決定ボタンを押したときの処理
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner = (Spinner)findViewById(R.id.categorySpinner);
                TextView textView = (TextView)findViewById(R.id.textView);

                categoryString = spinner.getSelectedItem().toString();
                textView.setText(categoryString);

                //databaseに値を入れる
                ContentValues values = new ContentValues();

                values.put("name", "test");
                values.put("category", categoryString);

                db.insert("requisitedb", null, values);
                readRequisiteData();
            }
        });
    }

    @Override
    public void onCategoryClick(int index){

        ContentValues values = new ContentValues();

        values.put("name", "test");
        values.put("category", sectionsPagerAdapter.getPageTitle(index).toString());

        Log.d("category", sectionsPagerAdapter.getPageTitle(index).toString());

        db.insert("requisitedb", null, values);
    }

    @Override
    public void onStop(){
        unregisterReceiver(mReceiver);
        super.onStop();
    }

    /**
     * トーストの生成
     * @param message
     * @param x
     * @param y
     */
    private void toastMake(String message, int x, int y){

        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        // 位置調整
        toast.setGravity(Gravity.CENTER, x, y);
        toast.show();
    }
}
