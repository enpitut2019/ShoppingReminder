package com.ji.shoppingreminder;

import com.google.android.material.tabs.TabLayout;
import com.ji.shoppingreminder.database.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.ji.shoppingreminder.ui.main.ViewAdapter;

import java.util.ArrayList;
import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MainActivity extends AppCompatActivity implements PlaceholderFragment.DBmanager, OnCheckedChangeListener {

    private final int REQUEST_PERMISSION = 1000;

    private int currentPage;
    private EditText editText;
    private ConstraintLayout registerLayout;
    private RequisiteDataBaseBuilder requisiteDBBuilder;
    private SQLiteDatabase db;

    private Switch backgroundSwitch;

    //通常モードのLayout
    private LinearLayout toolbarNormalLayout;
    //削除モードのLayout
    private ConstraintLayout toolbarDeleteLayout;

    private Button returnButton;
    private Button deleteButton;

    private LocationManager locationManager;
    private InputMethodManager inputMethodManager;
    private SectionsPagerAdapter sectionsPagerAdapter;

    /**
     * APIの確認とレシーバーの作成
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentPage = 0;
        //タブレイアウトの初期化
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        toolbarNormalLayout = findViewById(R.id.toolbarNormalLayout);
        toolbarDeleteLayout = findViewById(R.id.toolbarDeleteLayout);

        backgroundSwitch = findViewById(R.id.background_switch);
        backgroundSwitch.setOnCheckedChangeListener(this);
        //Serviceが起動中か確認
        if(isLocationServiceRunning()){
            backgroundSwitch.setChecked(true);
        }
        // LocationManager インスタンス生成
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        editText = findViewById(R.id.edit_text);
        registerLayout = findViewById(R.id.ConstraintLayout);

        InitializeDB();
        setViewListener(viewPager);
        setReturnButtonListener();
        setDeleteButtonListener();
        setAddButtonListener();
        changeEditTextHint();
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
    }

    /**
     * tabが変わったときの処理
     * @param viewPager
     */
    private void setViewListener(ViewPager viewPager){
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) { }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_SETTLING) {
                    if(toolbarDeleteLayout.getVisibility() == View.VISIBLE){
                        Fragment fragment = sectionsPagerAdapter.getCachedFragmentAt(currentPage);
                        ((PlaceholderFragment)fragment).viewAdapter.changeBooleanMode();
                        ContentValues values = new ContentValues();
                        values.put("deleteid", 0);
                        db.update("requisitedb", values, "deleteid = 1", null);
                        ((PlaceholderFragment)fragment).createRecyclerView();
                    }
                    //現在のページ数を更新する
                    currentPage = viewPager.getCurrentItem();
                    //EditTextのヒントを変更する
                    changeEditTextHint();
                }
            }
        });
    }

    /**
     * 戻るボタンを押したときの処理
     */
    private void setReturnButtonListener(){
        returnButton = findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changeMode(false);
                Fragment fragment = sectionsPagerAdapter.getCachedFragmentAt(currentPage);
                ((PlaceholderFragment)fragment).viewAdapter.changeBooleanMode();
                ((PlaceholderFragment)fragment).createRecyclerView();
            }
        });
    }

    /**
     * 削除ボタンを押したときの処理
     */
    private void setDeleteButtonListener(){
        deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItems();
                Fragment fragment = sectionsPagerAdapter.getCachedFragmentAt(currentPage);
                ((PlaceholderFragment)fragment).viewAdapter.changeBooleanMode();
                ((PlaceholderFragment)fragment).createRecyclerView();
            }
        });
    }

    /**
     * 登録ボタンをタップしたときの処理
     */
    private void setAddButtonListener(){
        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                String item = editText.getText().toString().trim();
                editText.getEditableText().clear();
                if (item.length() != 0) {
                    //editText内の文字をdatabaseに登録する
                    insertToDB(currentPage, item);
                    //recyclerViewの更新
                    Fragment fragment = sectionsPagerAdapter.getCachedFragmentAt(currentPage);
                    ((PlaceholderFragment)fragment).setList(getDBContents(currentPage));
                    ((PlaceholderFragment)fragment).viewAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * EditText内のヒントの文を変更する
     */
    private void changeEditTextHint(){
        String[] hint = getResources().getStringArray(R.array.category);
        editText.setHint(hint[currentPage]);
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
            } else {
                // それでも拒否された時の対応
                toastMake("位置情報を許可しないと施設の情報を取得できません", 0, 200);
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
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
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

        Cursor cursor = db.query(
                "requisitedb",
                new String[] {"notification"},
                "name = ? AND category = ?",
                new String[]{item, sectionsPagerAdapter.getPageTitle(index).toString()},
                null,
                null,
                null
        );

//        cursor.moveToFirst();
        if (cursor.getCount() == 0){
            ContentValues values = new ContentValues();

            values.put("name", item);
            values.put("category", sectionsPagerAdapter.getPageTitle(index).toString());
            values.put("notification", 1);
            values.put("deleteid",0);

            db.insert("requisitedb", null, values);
        } else {
            cursor.moveToFirst();
            if (cursor.getInt(0) == 0) {
                changeItemState(item, 1);
            } else {
                toastMake(item + "は既に追加されています", 0, 200);
            }
        }
    }

    /**
     * タグ毎のデータベース内のitemを取得する
     * @param index
     */
    @Override
    public List<String> getDBContents(int index){
        db = requisiteDBBuilder.getReadableDatabase();
        Log.d("debug","**********Cursor");
        //notificationの値の降順で並び替え
        String orderBy = "notification DESC";

        Cursor cursor = db.query(
                "requisitedb",
                new String[] { "name", "notification" },
                "category = ?",
                new String[]{sectionsPagerAdapter.getPageTitle(index).toString()},
                null,
                null,
                orderBy
        );
        cursor.moveToFirst();

        List<String> itemList = new ArrayList<String>();
        //name, notification のかたちでリストに格納する
        for (int i = 0; i < cursor.getCount(); i++) {
            itemList.add(cursor.getString(0) + "," + cursor.getInt(1));
            cursor.moveToNext();
        }
        // 忘れずに！
        cursor.close();
        return itemList;
    }

    /**
     * itemのnotificationの状態を取得する
     * @param item
     * @return
     */
    @Override
    public Boolean searchItem(String item){
        db = requisiteDBBuilder.getReadableDatabase();
        Log.d("debug","**********Cursor");

        Cursor cursor = db.query(
                "requisitedb",
                new String[] {"notification"},
                "name = ?",
                new String[]{item},
                null,
                null,
                null
        );

        cursor.moveToFirst();
        int notification = cursor.getInt(0);
        cursor.close();
        //通知できないように変更
        if(notification == 1){
            changeItemState(item, 0);
            //トーストを表示
            toastMake(item + "を購入しました", 0, 200);
            return false;
        }
        //通知できるように変更
        else{
            changeItemState(item, 1);
            return true;
        }
    }

    /**
     * 選択したアイテムを削除するように設定する
     * @param item
     */
    @Override
    public Boolean chooseDeleteItem(String item){
        ContentValues values = new ContentValues();
        Cursor cursor = db.query(
                "requisitedb",
                new String[] {"deleteid"},
                "name = ?",
                new String[]{item},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        int deleteid = cursor.getInt(0);
        cursor.close();
        if(deleteid == 1){
            values.put("deleteid", 0);
            db.update("requisitedb", values, "name = ?", new String[]{item});
            return false;
        }else{
            values.put("deleteid", 1);
            db.update("requisitedb", values, "name = ?", new String[]{item});
            return true;
        }
    }

    /**
     * データベースから選択したアイテムを削除する
     */
    public void deleteItems(){
        db = requisiteDBBuilder.getReadableDatabase();
        db.delete("requisitedb","deleteid = 1",null);
        Fragment fragment = sectionsPagerAdapter.getCachedFragmentAt(currentPage);
        ((PlaceholderFragment)fragment).setList(getDBContents(currentPage));
        ((PlaceholderFragment)fragment).viewAdapter.notifyDataSetChanged();
    }

    @Override
    public void changeMode(Boolean toDeleteMode){
        if(toDeleteMode){
            toolbarNormalLayout.setVisibility(View.GONE);
            toolbarDeleteLayout.setVisibility(View.VISIBLE);

            registerLayout.setVisibility(View.GONE);
        }
        else{
            toolbarDeleteLayout.setVisibility(View.GONE);
            toolbarNormalLayout.setVisibility(View.VISIBLE);

            registerLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * itemの通知許可状態を変更する
     * @param item
     * @param state
     */
    private void changeItemState(String item, int state){
        ContentValues values = new ContentValues();
        values.put("notification", state);
        db.update("requisitedb", values, "name = ?", new String[]{item});
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
        toast.setGravity(Gravity.BOTTOM, x, y);
        toast.show();
    }
}
