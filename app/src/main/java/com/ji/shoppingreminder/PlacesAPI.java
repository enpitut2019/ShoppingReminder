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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlacesAPI{

    private PlacesClient placesClient;
    private RequisiteDataBaseBuilder requisiteDataBaseBuilder;
    private SQLiteDatabase requisiteDB;
    private Context serviceContext;
    public Context activetyContext;
    private String gApiKey;
    private String[] category = {"食料品", "日用品", "衣料品"};

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
                    CategoryLists categoryLists = new CategoryLists();
                    LocationService locationService = new LocationService();
                    List<StringBuilder> storeList = new ArrayList<>();
                    for (int i = 0; i < 3; i++){
                        StringBuilder sb = new StringBuilder();
                        sb.append("店舗が見つかりました\n\n");
                        storeList.add(sb);
                    }
                    List<String> requisiteList = new ArrayList<>();
                    for (int i = 0; i < category.length; i++){
                        requisiteList.add(getRequisiteList(category[i]));
                    }

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

                        //買いたい物が買える施設か検索
                        if (requisiteList.get(0) != ""){
                            for(String storeCategory: categoryLists.foodStore){
                                String[] category = strBuf.toString().split(", ", -1);
                                for(int j = 0; j < category.length; j++){
                                    //買いたい物が買えるか判定
                                    if(category[j].equals(storeCategory)){
                                        storeList.set(0, storeList.get(0).append(pname + "\n"));
                                    }
                                }
                            }
                        }
                        if (requisiteList.get(1) != ""){
                            for(String storeCategory: categoryLists.groceryStore){
                                String[] category = strBuf.toString().split(", ", -1);
                                for(int j = 0; j < category.length; j++){
                                    //買いたい物が買えるか判定
                                    if(category[j].equals(storeCategory)){
                                        storeList.set(1, storeList.get(1).append(pname + "\n"));
                                    }
                                }
                            }
                        }
                        if (requisiteList.get(2) != ""){
                            for(String storeCategory: categoryLists.clothingStore){
                                String[] category = strBuf.toString().split(", ", -1);
                                for(int j = 0; j < category.length; j++){
                                    //買いたい物が買えるか判定
                                    if(category[j].equals(storeCategory)){
                                        storeList.set(2, storeList.get(2).append(pname + "\n"));
                                    }
                                }
                            }
                        }
                    }
                    for (int i = 0; i < requisiteList.size(); i++){
                        if (requisiteList.get(i) != ""){
                            locationService.sendNotification(context, category[i] + " : " + requisiteList.get(i), storeList.get(i).toString(), 10 * (i + 1));
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
     * デーだベースから買いたい物をカテゴリを指定して抽出
     * @param category
     * @return 買いたい物が入った文字列
     */
    public String getRequisiteList(String category){
        StringBuilder buff = new StringBuilder();
        Cursor cursor = requisiteDB.query(
                "requisitedb",
                new String[] { "name", "category" },
                "category = ?",
                new String[] {category},
                null,
                null,
                null
        );
        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            buff.append(cursor.getString(0));
            if (i != cursor.getCount() - 1) {
                buff.append(", ");
            }
        }

        return buff.toString();
    }
}
