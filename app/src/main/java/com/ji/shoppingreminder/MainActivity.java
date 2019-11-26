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
import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.util.Log;

import com.ji.shoppingreminder.database.RequisiteDataBaseBuilder;
import com.ji.shoppingreminder.ui.main.PlaceholderFragment;
import com.ji.shoppingreminder.ui.main.SectionsPagerAdapter;

import java.util.ArrayList;
import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MainActivity extends AppCompatActivity implements PlaceholderFragment.DBmanager, OnCheckedChangeListener {

    private static final int REQUEST_MULTI_PERMISSIONS = 101;
    private String categoryString;

    private final int REQUEST_PERMISSION = 1000;
    private static final int REQUEST = 1;

    private RequisiteDataBaseBuilder requisiteDBBuilder;
    private SQLiteDatabase db;
    private StoreDataBaseBuilder storeDataBaseBuilder;
    private SQLiteDatabase storeDB;
    private Switch backgroundSwitch;
    LocationManager locationManager;

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

        backgroundSwitch = findViewById(R.id.background_switch);
        backgroundSwitch.setOnCheckedChangeListener(this);
        //Serviceが起動中か確認
        if(isLocationServiceRunning()){
            backgroundSwitch.setChecked(true);
        }
        // LocationManager インスタンス生成
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //位置情報が許可されていなかったら許可を求める
//        if(!checkPermission()){
//            requestLocationPermission();
//        }

        InitializeDB();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
     * LocationServiceが起動中か確認する
     * @return
     */
    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getApplication().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService.class.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 位置情報の許可を確認する
     */
    public Boolean checkPermission() {
        // 既に許可している
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        // 拒否していた場合
        else{
            return false;
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
                startService();
            } else {
                // それでも拒否された時の対応

                toastMake("位置情報を許可しないと施設の情報を取得できません", 0, -200);
            }
        }
    }

    /**
     * backgroundSwitchが押されたときの処理
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked){
            final boolean gpsEnabled
                    = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            //アプリの位置情報が許可されていない
            if(!checkPermission()){
                requestLocationPermission();
                buttonView.setChecked(false);
            }
            //端末の位置情報が許可されていない
            else if(!gpsEnabled){
                // GPSを設定するように促す
                enableLocationSettings();
                buttonView.setChecked(false);
            }
            //サービスの起動
            else{
                startService();
            }
        }
        else{
            // Serviceの停止
            Intent intent = new Intent(getApplication(), LocationService.class);
            stopService(intent);
        }
    }

    /**
     * GPSがoffのときにGPSの設定を表示する
     */
    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    private void startService(){
        Intent intent = new Intent(getApplication(), LocationService.class);
        // API 26 以降
        startForegroundService(intent);
        backgroundSwitch.setChecked(true);
    }

    /**
     * 登録ボタンが押されたときに実行され、itemをデータベースに登録する
     * @param index
     * @param item
     */
    @Override
    public void insertToDB(int index, String item){

        ContentValues values = new ContentValues();

        values.put("name", item);
        values.put("category", sectionsPagerAdapter.getPageTitle(index).toString());

        Log.d("category", sectionsPagerAdapter.getPageTitle(index).toString());

        db.insert("requisitedb", null, values);
    }

    /**
     * タグ毎のデータベース内のitemを表示する
     * @param textView
     * @param index
     */
    @Override
    public void displayDBContents(TextView textView, int index){
        db = requisiteDBBuilder.getReadableDatabase();
        Log.d("debug","**********Cursor");

        Cursor cursor = db.query(
                "requisitedb",
                new String[] { "name", "category" },
                "category = ?",
                new String[]{sectionsPagerAdapter.getPageTitle(index).toString()},
                null,
                null,
                null
        );

        cursor.moveToFirst();

        StringBuilder sbuilder = new StringBuilder();

        for (int i = 0; i < cursor.getCount(); i++) {
            sbuilder.append(cursor.getString(0));
            sbuilder.append("\n");
            cursor.moveToNext();
        }

        // 忘れずに！
        cursor.close();

        Log.d("debug","**********"+sbuilder.toString());
        textView.setText(sbuilder.toString());
    }

    @Override
    public void onStop(){
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
