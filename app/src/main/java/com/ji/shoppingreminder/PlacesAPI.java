package com.ji.shoppingreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.ji.shoppingreminder.database.CategoryLists;
import com.ji.shoppingreminder.database.StoreDataBaseBuilder;

import java.util.ArrayList;
import java.util.List;

public class PlacesAPI {

    private PlacesClient placesClient;
    private StoreDataBaseBuilder storeDataBaseBuilder;
    private SQLiteDatabase storeDB;
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
        if(storeDataBaseBuilder == null){
            storeDataBaseBuilder = new StoreDataBaseBuilder(serviceContext);
        }
        if(storeDB == null){
            storeDB = storeDataBaseBuilder.getWritableDatabase();
        }
    }

    /**
     * GooglePlacesAPIで周辺施設の情報を取得
     */
    public  void GetLocationInfo(){
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
                    //以前のデータを全削除
                    storeDB.delete("storedb", null, null);
                    int size = response.getPlaceLikelihoods().size();
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

                        Log.d("test", pname);
                        if(CheckCategories(strBuf.toString())){
                            Log.d("test", "write");
                            WriteToDatabase(pname, latitude, longitude, strBuf.toString());
                        }
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
    private boolean CheckCategories(String categories){
        String[] category = categories.split(", ", -1);
        CategoryLists categoryLists = new CategoryLists();
        for(int i = 0; i < category.length; i++){
            //groceryが買えるかどうかを判定
            for(String storeCategory: categoryLists.groceryStore){
                if(category[i].equals(storeCategory)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 施設の情報をデータベースに書き込む
     * @param pname 施設の名前
     * @param latitude 緯度
     * @param longitude 経度
     * @param categories 施設のカテゴリ
     */
    private void WriteToDatabase(String pname, double latitude, double longitude, String categories){
        ContentValues values = new ContentValues();
        values.put("storeName", pname);
        values.put("latitude", String.valueOf(latitude));
        values.put("longitude", String.valueOf(longitude));

        values.put("category",categories);

        storeDB.insert("storedb", null, values);
    }
}
