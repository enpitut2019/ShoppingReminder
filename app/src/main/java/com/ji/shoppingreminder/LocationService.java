package com.ji.shoppingreminder;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.Notification;
import android.app.Notification.Style;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.util.Random;

public class LocationService extends Service implements LocationListener{

    private LocationManager locationManager;
    private Context context;

    private static final int MinTime = 1000;
    private static final float MinDistance = 50;

    private static NotificationManager notificationManager;

    Intent intent;
    PlacesAPI placesAPI;

    @Override
    public void onCreate(){
        super.onCreate();

        context = getApplicationContext();

        // LocationManager インスタンス生成
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        placesAPI = new PlacesAPI(context, String.valueOf(R.string.places_api_key));
        placesAPI.InitializeStoreDB();
    }

    /**
     *フォアグラウンドサービスを開始する
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        int requestCode = 0;
        String channelId = "background";
        this.intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, requestCode,
                        this.intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //API26以上
        if(Build.VERSION.SDK_INT >= 26){

            // ForegroundにするためNotificationが必要、Contextを設定
            notificationManager =
                    (NotificationManager)context.
                            getSystemService(Context.NOTIFICATION_SERVICE);

            // Notification　Channel 設定
            NotificationChannel channel = new NotificationChannel(
                    channelId, "GPSの起動を通知" , NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Silent Notification");
            // 通知音を消さないと毎回通知音が出てしまう
            // この辺りの設定はcleanにしてから変更
            channel.setSound(null,null);
            // 通知ランプを消す
            channel.enableLights(false);
            channel.setLightColor(Color.BLUE);
            // 通知バイブレーション無し
            channel.enableVibration(false);

            if(notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(context, channelId)
                        .setContentTitle("GPS作動中です")
                        // アイコン設定
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .build();

                // startForeground
                startForeground(1, notification);
            }
        }
        //API26以前
        else{
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("GPS作動中です")
                    .setContentText("")
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            startForeground(1, notificationBuilder.build());
        }
        startGPS();

        return START_NOT_STICKY;
    }

    /**
     * 位置情報の取得を開始する
     */
    protected void startGPS() {
        if (locationManager != null) {
            try {
                Log.d("test", "startGPS");
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)!=
                        PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MinTime, MinDistance, this);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MinTime, MinDistance, this);
            } catch (Exception e) {
                Log.d("test", "cannot start GPS");
                e.printStackTrace();
            }
        } else {

        }
    }

    /**
     * 位置情報が変わったときに呼び出される関数
     * @param location the value of location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d("test", "getLocation");
        StringBuilder strBuf = new StringBuilder();

        strBuf.append("----------\n");

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        String str = "Latitude = " +String.valueOf(location.getLatitude()) + "\n";
        strBuf.append(str);

        str = "Longitude = " + String.valueOf(location.getLongitude()) + "\n";
        strBuf.append(str);

        str = "Accuracy = " + String.valueOf(location.getAccuracy()) + "\n";
        strBuf.append(str);

        str = "Altitude = " + String.valueOf(location.getAltitude()) + "\n";
        strBuf.append(str);

//        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss");
//        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
//        String currentTime = sdf.format(location.getTime());

//        str = "Time = " + currentTime + "\n";
//        strBuf.append(str);

        str = "Speed = " + String.valueOf(location.getSpeed()) + "\n";
        strBuf.append(str);

        str = "Bearing = " + String.valueOf(location.getBearing()) + "\n";
        strBuf.append(str);

        strBuf.append("----------\n");

        placesAPI.GetLocationInfo(context);
    }

    /**
     * PUSH通知を送信する
     */
    public static void sendNotification(Context context, String requisiteList, String storeList, int index){
        Icon icon = null;
        switch (index){
            case 10:
                icon = Icon.createWithResource(context, R.drawable.ic_stat_food);
                break;
            case 20:
                icon = Icon.createWithResource(context, R.drawable.ic_stat_grocery);
                break;
            case 30:
                icon = Icon.createWithResource(context, R.drawable.ic_stat_clothing);
                break;
            default:
                break;
        }

        String channelId = "default";
        //通知をタップしたときに開くアクティビティー
        Intent  mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int notificationId = index;
        //API26以上
        if(Build.VERSION.SDK_INT >= 26){

            // Notification　Channel 設定
            NotificationChannel channel = new NotificationChannel(
                    channelId, "リストと店舗の通知" , NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            //バイブレーションのパターンを設定
            //new long[]{delay, play, sleep, play, sleep, ...}
            channel.setVibrationPattern(new long[]{0, 300, 300, 400});
            if(notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle()
                        .setBigContentTitle(requisiteList)
                        .bigText(storeList);
                Notification notification = new Notification.Builder(context, channelId)
                        .setContentTitle(requisiteList)
                        .setContentText(storeList)
                        // アイコン設定
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setLargeIcon(icon)
                        .setAutoCancel(true)
                        //通知をタップしたときに開くアクティビティー
                        .setContentIntent(pendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .setStyle(bigTextStyle)
                        .build();
                notificationManager.notify(notificationId, bigTextStyle.build());
            }
        }
        else{
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(requisiteList)
                    .setContentText(storeList)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            notificationBuilder.build();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Android 6, API 23以上でパーミッシンの確認
        if(Build.VERSION.SDK_INT <= 28){
            StringBuilder strBuf = new StringBuilder();

            switch (status) {
                case LocationProvider.AVAILABLE:
                    //strBuf.append("LocationProvider.AVAILABLE\n");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    strBuf.append("LocationProvider.OUT_OF_SERVICE\n");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    strBuf.append("LocationProvider.TEMPORARILY_UNAVAILABLE\n");
                    break;
            }
        }
    }

    /**
     * 位置情報の取得を終了する
     */
    private void stopGPS(){
        if (locationManager != null) {
            // update を止める
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopGPS();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
