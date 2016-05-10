package com.ggface.achivetricks.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;

import com.ggface.achivetricks.App;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "pHunters.db";
    public static final String TABLE_NAME = "persons";
    private static DBHelper theDb;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY" +
                    ", person_name TEXT" +
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

    public void reCreate() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("drop table " + TABLE_NAME);
            db.execSQL("CREATE TABLE persons (_id INTEGER PRIMARY KEY" +
                    ", person_name TEXT" +
                    ", pussy INTEGER" +
                    ", anal INTEGER" +
                    ", oral INTEGER" +
                    ", girl_photo BLOB);");
        } catch (SQLException e) {
            String msg = e.getMessage();
            App.logD("DBHelper onCreate", msg);
        }
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (theDb == null) {
            // Make sure that we do not leak Activity's context
            theDb = new DBHelper(context.getApplicationContext());
        }

        return theDb;
    }

    public long insert(Person instance) {
        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = model.getValues();

//        String sql = "insert into "
//                + TABLE_NAME
//                + " (person_name, pussy, oral, anal) "
//                + "values(" + instance.name + ", " + (instance.traditional ? 1 : 0) + ", "
//                + (instance.oral ? 1 : 0) + ", " + (instance.anal ? 1 : 0) + ")";
//        App.logD("insert", sql);
//        db.execSQL(sql);
//        long returnCode = db.insert(TABLE_NAME, null, values);
//        Log.d(DBHelper.class.getSimpleName(), "Row added at " + returnCode
//                + " of " + model.getTableName());

//        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME
//                + " WHERE _id=last_insert_rowid()", new String[]{});
//        if (cursor != null) {
//            cursor.moveToNext();
//            id = cursor.getInt(cursor.getColumnIndex("_id"));
//            cursor.close();
//        }

        db.insert(TABLE_NAME, null, instance.toDB());
        db.close();
        return -1;
    }

    public long update(Person instance) {
        ContentValues values = new ContentValues();
        // Задайте значения для каждого столбца
        values.put("person_name", instance.name);
        values.put("pussy", instance.traditional ? 1 : 0);
        values.put("oral", instance.oral ? 1 : 0);
        values.put("anal", instance.anal ? 1 : 0);
        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = model.getValues();

        String[] args = new String[]{String.valueOf(instance.id)};
        db.update(TABLE_NAME, values, "_id=?", args);

        db.close();
        return -1;
    }

    public List<Person> read() {
        SQLiteDatabase db = this.getWritableDatabase();
        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        Cursor c;
        try {
//            c = db.query("GIRLS", new String[] {"PH_NAME", "PH_CUNT", "PH_ASS", "PH_MINET", "PH_PHOTO"}, null, null, null, null, null);
//            c = db.query("GIRLS", null, null, null, null, null, null);
            c = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_NAME, null);
        } catch (Exception e) {
            return null;
        }
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        List<Person> list = new ArrayList<>();
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("_id");
            int nameColIndex = c.getColumnIndex("person_name");
            int pussyColIndex = c.getColumnIndex("pussy");
            int analColIndex = c.getColumnIndex("anal");
            int oralColIndex = c.getColumnIndex("oral");
            int photoColIndex = c.getColumnIndex("girl_photo");

            do {
                Person item = new Person();
                item.id = c.getInt(idColIndex);
                item.name = c.getString(nameColIndex);

                item.traditional = c.getInt(pussyColIndex) == 1;
                item.anal = c.getInt(analColIndex) == 1;
                item.oral = c.getInt(oralColIndex) == 1;

                byte[] byteArray = c.getBlob(photoColIndex);
                if (byteArray != null)
                    item.image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                // получаем значения по номерам столбцов и пишем все в лог
//                Log.d(LOG_TAG,
//                        "ID = " + c.getInt(idColIndex) +
//                                ", name = " + c.getString(nameColIndex) +
//                                ", email = " + c.getString(emailColIndex));
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
                list.add(item);
            } while (c.moveToNext());
        } else
//            Log.d(LOG_TAG, "0 rows");
            c.close();
        return list;
    }

    public Person select(long id) {
        Person item = null;
        SQLiteDatabase db = this.getWritableDatabase();
        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        Cursor c;
        try {
//            c = db.query("GIRLS", new String[] {"PH_NAME", "PH_CUNT", "PH_ASS", "PH_MINET", "PH_PHOTO"}, null, null, null, null, null);
//            c = db.query("GIRLS", null, null, null, null, null, null);
            c = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_NAME + " WHERE _id = " + id, null);
        } catch (Exception e) {
            return null;
        }
//        UI.text(getActivity(), "count: " + c.getCount());
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
//            List<Person> list = new ArrayList<>();
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("_id");
            int nameColIndex = c.getColumnIndex("person_name");
            int pussyColIndex = c.getColumnIndex("pussy");
            int analColIndex = c.getColumnIndex("anal");
            int oralColIndex = c.getColumnIndex("oral");
            int photoColIndex = c.getColumnIndex("girl_photo");
            item = new Person();
            item.id = c.getInt(idColIndex);
            item.name = c.getString(nameColIndex);

            item.traditional = c.getInt(pussyColIndex) == 1;
            item.anal = c.getInt(analColIndex) == 1;
            item.oral = c.getInt(oralColIndex) == 1;

            byte[] byteArray = c.getBlob(photoColIndex);
            if (byteArray != null)
                item.image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }


        c.close();
        return item;

    }
}