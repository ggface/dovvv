package com.ggface.dovvv.classes;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.ggface.dovvv.App;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper implements IRoom {

    /**
     * Базовая версия БД
     */
    public static final int VERSION_1 = 1;

    /**
     * Добавил поле для сортировки
     */
    public static final int VERSION_2 = 2;

    public static final String DATABASE_NAME = "pHunters.db";
    public static final String TABLE_NAME = "persons";
    private static DBHelper theDb;

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY" +
                    ", person_name TEXT" +
                    ", pussy INTEGER" +
                    ", anal INTEGER" +
                    ", oral INTEGER" +
                    ", ext TEXT);");
        } catch (SQLException e) {
            String msg = e.getMessage();
            App.logD("DBHelper onCreate", msg);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < VERSION_2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN position INTEGER DEFAULT 0");

            List<Person> personList = read();
            for (int i = 0; i < personList.size(); i++) {
                personList.get(i).mPosition = i;
                update(personList.get(i));
            }
        }
    }

    public static synchronized DBHelper getInstance(Context context) {
        if (theDb == null) {
            // Make sure that we do not leak Activity's context
            theDb = new DBHelper(context.getApplicationContext());
        }

        return theDb;
    }

    @NonNull
    @Override
    public List<Person> read() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c;
        try {
            c = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_NAME, null);
        } catch (Exception e) {
            return Collections.emptyList();
        }

        List<Person> list = new ArrayList<>();
        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex("_id");
            int nameColIndex = c.getColumnIndex("person_name");
            int pussyColIndex = c.getColumnIndex("pussy");
            int analColIndex = c.getColumnIndex("anal");
            int oralColIndex = c.getColumnIndex("oral");
            int extColIndex = c.getColumnIndex("ext");

            do {
                Person item = new Person();
                item.id = c.getInt(idColIndex);
                item.name = c.getString(nameColIndex);

                item.traditional = c.getInt(pussyColIndex) == 1;
                item.anal = c.getInt(analColIndex) == 1;
                item.oral = c.getInt(oralColIndex) == 1;
                item.extension = c.getString(extColIndex);

                list.add(item);
            } while (c.moveToNext());
        }

        c.close();

        Collections.sort(list, (person1, person2) -> Integer.compare(person1.mPosition, person2.mPosition));
        return list;
    }

    @Override
    public Person select(long id) {
        Person item = null;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c;
        try {
            c = db.rawQuery("SELECT * FROM " + DBHelper.TABLE_NAME + " WHERE _id = " + id, null);
        } catch (Exception e) {
            return null;
        }

        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex("_id");
            int nameColIndex = c.getColumnIndex("person_name");
            int pussyColIndex = c.getColumnIndex("pussy");
            int analColIndex = c.getColumnIndex("anal");
            int oralColIndex = c.getColumnIndex("oral");
            int extColIndex = c.getColumnIndex("ext");
            int posColIndex = c.getColumnIndex("position");
            item = new Person();
            item.id = c.getInt(idColIndex);
            item.mPosition = c.getInt(posColIndex);
            item.name = c.getString(nameColIndex);

            item.traditional = c.getInt(pussyColIndex) == 1;
            item.anal = c.getInt(analColIndex) == 1;
            item.oral = c.getInt(oralColIndex) == 1;
            item.extension = c.getString(extColIndex);
        }

        c.close();
        return item;
    }

    @Override
    public long insert(Person instance) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = db.insert(TABLE_NAME, null, instance.toDB());
        db.close();
        return id;
    }

    @Override
    public void update(Person person) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[]{String.valueOf(person.id)};
        db.update(TABLE_NAME, person.toDB(), "_id=?", args);
        db.close();
    }

    public void clear() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME);
        db.close();
    }

    @Override
    public void remove(Person instance) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE _id=?", args(instance.id));//TODO проверить работу фции args
        db.close();
    }

    private Object[] args(Object... values) {
        if (null == values) {
            return null;
        }

        List<String> list = new ArrayList<>();
        for (Object value : values) {
            list.add(String.valueOf(value));
        }
        return list.toArray();
    }
}