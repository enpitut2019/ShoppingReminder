package com.ji.shoppingreminder.database;

import java.util.List;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RequisiteDataBaseBuilder extends SQLiteOpenHelper {

    // データーベースのバージョン
    public static final int DATABASE_VERSION = 1;
    // データーベース名
    public static final String DATABASE_NAME = "RequisiteDB.db";
    private static final String TABLE_NAME = "requisitedb";
    private static final String _ID = "_id";
    private static final String COLUMN_NAME_TITLE = "name";
    private static final String COLUMN_NAME_SUBTITLE = "category";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_TITLE + " TEXT," +
                    COLUMN_NAME_SUBTITLE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public RequisiteDataBaseBuilder(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // テーブル作成
        db.execSQL(
                SQL_CREATE_ENTRIES
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        // アップデートの判別、古いバージョンは削除して新規作成
        db.execSQL(
                SQL_DELETE_ENTRIES
        );
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db,
                            int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
