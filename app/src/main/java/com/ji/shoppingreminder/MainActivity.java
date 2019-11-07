package com.ji.shoppingreminder;

import com.ji.shoppingreminder.database.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.Place.Field;
import com.google.android.libraries.places.api.model.Place.Type;
import com.google.android.libraries.places.api.model.*;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.ji.shoppingreminder.database.RequisiteDataBaseBuilder;

import java.util.ArrayList;
import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_MULTI_PERMISSIONS = 101;

    private BroadcastReceiver mReceiver = null;
    private IntentFilter mIntentFilter = null;
    private TextView textView;
    private String categoryString;
    private PlacesClient placesClient;

    private final int REQUEST_PERMISSION = 1000;
    private static final int REQUEST = 1;

    private RequisiteDataBaseBuilder requisiteDBBuilder;
    private SQLiteDatabase db;
    private StoreDataBaseBuilder storeDataBaseBuilder;
    private SQLiteDatabase storeDB;

    /**
     * APIの確認とレシーバーの作成
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = getApplicationContext();

        initSpinners();

        textView = findViewById(R.id.log_text);

        // Android 6, API 23以上でパーミッシンの確認
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

        placesAPI();
        InitializeDB();
    }

    private void placesAPI(){
        if (!Places.isInitialized()) {
            String gApiKey = this.getString(R.string.places_api_key);
            Places.initialize(this, gApiKey);
        }
        //データベースの初期化
        if(storeDataBaseBuilder == null){
            storeDataBaseBuilder = new StoreDataBaseBuilder(getApplicationContext());
        }
        if(storeDB == null){
            storeDB = storeDataBaseBuilder.getWritableDatabase();
        }

        storeDataBaseBuilder.onUpgrade(storeDB, 1, 2);
        // Retrieve a PlacesClient
        placesClient = Places.createClient(this);

        List<Place.Field> fields = new ArrayList<>();
        fields.add(Place.Field.NAME);
        fields.add(Place.Field.LAT_LNG);
        fields.add(Field.TYPES);

        FindCurrentPlaceRequest currentPlaceRequest =
                FindCurrentPlaceRequest.newInstance(fields);
        Task<FindCurrentPlaceResponse> currentPlaceTask =
                placesClient.findCurrentPlace(currentPlaceRequest);

        currentPlaceTask.addOnSuccessListener(
                (response) -> {
                    int size = response.getPlaceLikelihoods().size();
                    for(int i = 0; i < size; i++){
                        ContentValues values = new ContentValues();
                        String pname = response.getPlaceLikelihoods().get(i).getPlace().getName();
                        values.put("storeName", pname);
                        double latitude = response.getPlaceLikelihoods().get(i).getPlace().getLatLng().latitude;
                        double longitude = response.getPlaceLikelihoods().get(i).getPlace().getLatLng().longitude;
                        values.put("latitude", String.valueOf(latitude));
                        values.put("longitude", String.valueOf(longitude));
                        String type = response.getPlaceLikelihoods().get(i).getPlace().getTypes().toString();
                        //最初と最後の文字を削除する
                        StringBuilder strBuf = new StringBuilder();
                        strBuf.append(type);
                        strBuf.deleteCharAt(0);
                        strBuf.setLength(strBuf.length() - 1);
                        values.put("category", strBuf.toString());

                        textView.setText(strBuf.toString());
                    }
                })
                .addOnFailureListener(
                        (exception) -> {
                            exception.printStackTrace();
                            textView.setText("failed");
                        });
    }

    private void InitializeDB(){
        if(requisiteDBBuilder == null){
            requisiteDBBuilder = new RequisiteDataBaseBuilder(getApplicationContext());
        }
        if(db == null){
            db = requisiteDBBuilder.getWritableDatabase();
        }

        ContentValues values = new ContentValues();

        values.put("title", "potate");
        values.put("subtitle", "food");

        db.insert("requisitedb", null, values);

    }

    private void readRequisiteData(){
        if(requisiteDBBuilder == null){
            requisiteDBBuilder = new RequisiteDataBaseBuilder(getApplicationContext());
        }

        if(db == null){
            db = requisiteDBBuilder.getReadableDatabase();
        }
        Log.d("debug","**********Cursor");

        Cursor cursor = db.query(
                "requisitedb",
                new String[] { "title", "subtitle" },
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
            sbuilder.append(cursor.getInt(1));
            sbuilder.append("\n");
            cursor.moveToNext();
        }

        // 忘れずに！
        cursor.close();

        Log.d("debug","**********"+sbuilder.toString());
        textView.setText(sbuilder.toString());
    }

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

    @Override
    protected void onStart() {
        super.onStart();

        Button btn = (Button)findViewById(R.id.categoryDecideButton);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner = (Spinner)findViewById(R.id.categorySpinner);
                TextView textView = (TextView)findViewById(R.id.textView);

                categoryString = spinner.getSelectedItem().toString();
                textView.setText(categoryString);
            }
        });
    }


    public void initSpinners() {
        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        String[] labels = getResources().getStringArray(R.array.category);
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, labels);
        categorySpinner.setAdapter(adapter);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.log_text);


        Button buttonStart = findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), LocationService.class);

                // API 26 以降
                //startForegroundService(intent);

                //textView.setText(R.string.start);
                //readRequisiteData();
                readStoreData();
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
