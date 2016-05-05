package com.ggface.achivetricks.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ggface.achivetricks.App;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "pHunters.db";

    private static DBHelper theDb;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE girls (id INTEGER PRIMARY KEY AUTOINCREMENT" +
                    ", girl_name TEXT" +
                    ", pussy INTEGER" +
                    ", anal INTEGER" +
                    ", oral INTEGER" +
                    ", girl_photo BLOB);");
        } catch (SQLException e) {
            String msg = e.getMessage();
            App.logD("DBHelper onCreate", msg);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static synchronized DBHelper getInstance(Context context) {
        if (theDb == null) {
            // Make sure that we do not leak Activity's context
            theDb = new DBHelper(context.getApplicationContext());
        }

        return theDb;
    }

    public int add(ContentValues values) {
        int id = -1;
        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = model.getValues();
        values.remove("id");
        long returnCode = db.insert("girls", null, values);
//        Log.d(DBHelper.class.getSimpleName(), "Row added at " + returnCode
//                + " of " + model.getTableName());

        Cursor cursor = db.rawQuery("SELECT * FROM girls"
                + " WHERE id=last_insert_rowid()", new String[]{});
        if (cursor != null) {
            cursor.moveToNext();
            id = cursor.getInt(cursor.getColumnIndex("id"));
            cursor.close();
        }

        db.close();
        return id;
    }
}