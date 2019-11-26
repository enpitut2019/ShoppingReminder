package com.ji.shoppingreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.ji.shoppingreminder.database.CategoryLists;
import com.ji.shoppingreminder.database.RequisiteDataBaseBuilder;
import com.ji.shoppingreminder.database.StoreDataBaseBuilder;

import java.util.ArrayList;
import java.util.List;

public class PlacesAPI{

    private PlacesClient placesClient;
    private RequisiteDataBaseBuilder requisiteDataBaseBuilder;
    private SQLiteDatabase requisiteDB;
    private Context serviceContext;
    public Context activetyContext;
    private String gApiKey;

    public PlacesAPI(Context serviceContext, String gApiKey){
        this.serviceContext = serviceContext;
        this.gApiKey = gApiKey;
    }

    /**
     * ストアデータベースの初期化
     */
    public void InitializeStoreDB(){
        //買いたい物の情報を格納するデータベースの初期化
        if(requisiteDataBaseBuilder == null){
            requisiteDataBaseBuilder = new RequisiteDataBaseBuilder(serviceContext);
        }
        if(requisiteDB == null){
            requisiteDB = requisiteDataBaseBuilder.getReadableDatabase();
        }
    }

    /**
     * GooglePlacesAPIで周辺施設の情報を取得
     */
    public  void GetLocationInfo(Context context){
        if (!Places.isInitialized()) {
            Places.initialize(serviceContext, serviceContext.getString(R.string.places_api_key));
        }

        placesClient = Places.createClient(serviceContext);

        List<Place.Field> fields = new ArrayList<>();
        fields.add(Place.Field.NAME);
        fields.add(Place.Field.LAT_LNG);
        fields.add(Place.Field.TYPES);

        FindCurrentPlaceRequest currentPlaceRequest =
                FindCurrentPlaceRequest.newInstance(fields);
        Task<FindCurrentPlaceResponse> currentPlaceTask =
                placesClient.findCurrentPlace(currentPlaceRequest);


        currentPlaceTask.addOnSuccessListener(
                (response) -> {
                    int size = response.getPlaceLikelihoods().size();
                    LocationService locationService = new LocationService();
                    StringBuilder notificationMessage = new StringBuilder();
                    Log.d("test", "start");
                    for(int i = 0; i < size; i++){
                        String pname = response.getPlaceLikelihoods().get(i).getPlace().getName();
                        double latitude = response.getPlaceLikelihoods().get(i).getPlace().getLatLng().latitude;
                        double longitude = response.getPlaceLikelihoods().get(i).getPlace().getLatLng().longitude;
                        String categories = response.getPlaceLikelihoods().get(i).getPlace().getTypes().toString();

                        //最初と最後の文字を削除する
                        StringBuilder strBuf = new StringBuilder();
                        strBuf.append(categories);
                        strBuf.deleteCharAt(0);
                        strBuf.setLength(strBuf.length() - 1);

                        List<String> requisites = CheckCategories(strBuf.toString());
                        Log.d("test", pname);
                        Log.d("test", strBuf.toString());
                        //Log.d("test", String.valueOf(requisites.size()));
                        if(requisites.size() != 0){
                            Log.d("test", "write");
                            notificationMessage.append(pname + "で" + requisites.toString() + "が購入できます\n");
                        }
                    }
                    if(notificationMessage.length() != 0){
                        locationService.sendNotification(context, notificationMessage.toString());
                    }
                })
                .addOnFailureListener(
                        (exception) -> {
                            Log.d("test", exception.getLocalizedMessage());
                            exception.printStackTrace();
                        });
    }

    /**
     * 施設のカテゴリが買いたいものを買える場所か確認する
     * @param categories 施設のカテゴリ
     * @return 施設の情報をデータベースに保存するかどうか
     */
    private List<String> CheckCategories(String categories){
        Cursor cursor = requisiteDB.query(
                "requisitedb",
                new String[] { "name", "category" },
                null,
                null,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        CategoryLists categoryLists = new CategoryLists();
        List<String> requisites = new ArrayList<String>();
        for (int i = 0; i < cursor.getCount(); i++) {
            //買いたい物のカテゴリ
            String requisiteCategory = cursor.getString(1);
            switch(requisiteCategory){
                case "食料品":
                    for(String storeCategory: categoryLists.foodStore){
                        String[] category = categories.split(", ", -1);
                        for(int j = 0; j < category.length; j++){
                            //買いたい物が買えるか判定
                            if(category[j].equals(storeCategory)){
                                requisites.add(cursor.getString(0));
                            }
                        }
                    }
                    break;
                case "日用品":
                    for(String storeCategory: categoryLists.groceryStore){
                        String[] category = categories.split(", ", -1);
                        for(int j = 0; j < category.length; j++){
                            //買いたい物が買えるか判定
                            if(category[j].equals(storeCategory)){
                                requisites.add(cursor.getString(0));
                            }
                        }
                    }
                    break;
                case "衣料品":
                    for(String storeCategory: categoryLists.clothingStore){
                        String[] category = categories.split(", ", -1);
                        for(int j = 0; j < category.length; j++){
                            //買いたい物が買えるか判定
                            if(category[j].equals(storeCategory)){
                                requisites.add(cursor.getString(0));
                            }
                        }
                    }
                    break;
                default:
                    Log.d("test", "check");
                    break;
            }
            cursor.moveToNext();
        }

        // 忘れずに！
        cursor.close();
        return requisites;
    }
}
