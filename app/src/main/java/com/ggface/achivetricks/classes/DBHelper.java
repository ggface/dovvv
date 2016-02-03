package com.ggface.achivetricks.classes;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ggface.achivetricks.UI;

/**
 * Created by ggface on 03.02.16.
 */
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "pHunters.ach", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE GIRLS (id INTEGER PRIMARY KEY AUTOINCREMENT" +
                    ", girl_name TEXT,pussy INTEGER" +
                    ", anal INTEGER" +
                    ", oral INTEGER" +
                    ", girl_photo BLOB);");
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (null == msg) {
                msg = "";
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}