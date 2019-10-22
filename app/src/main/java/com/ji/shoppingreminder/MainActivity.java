package com.ji.shoppingreminder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_MULTI_PERMISSIONS = 101;

    private TextView textView;

    private final int REQUEST_PERMISSION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = getApplicationContext();

        textView = findViewById(R.id.log_text);

        // Android 6, API 23以上でパーミッシンの確認
        if(Build.VERSION.SDK_INT >= 23){
            textView.setText("API23 over");
            checkPermission();
        }
        else{
            startLocationService();
        }

    }

    // 位置情報許可の確認
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

    // 許可を求める
    private void requestLocationPermission() {
            ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSION);
    }

    // 結果の受け取り
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

    private void startLocationService() {
        setContentView(R.layout.activity_main);

        //textView = findViewById(R.id.log_text);

        Button buttonStart = findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getApplication(), LocationService.class);

                // API 26 以降
                //startForegroundService(intent);

                textView.setText(R.string.start);
                // Activityを終了させる
                finish();
            }
        });

        Button buttonLog = findViewById(R.id.button_log);
        buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //textView.setText(fileReadWrite.readFile());
                textView.setText(R.string.log);
            }
        });

        Button buttonReset = findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Serviceの停止
                //Intent intent = new Intent(getApplication(), LocationService.class);
                //stopService(intent);

                //fileReadWrite.clearFile();
                textView.setText(R.string.reset);
            }
        });
    }

    //トーストの生成
    private void toastMake(String message, int x, int y){

        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        // 位置調整
        toast.setGravity(Gravity.CENTER, x, y);
        toast.show();
    }
}
