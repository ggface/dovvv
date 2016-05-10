package com.ggface.achivetricks.classes;

import android.content.ContentValues;
import android.graphics.Bitmap;

import java.util.Date;

public class Person {

    public long id;
    public String filename;
    public String name;
    public boolean oral, anal, traditional;
    public Date addDate;
    public Bitmap image;

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    public ContentValues toDB() {
        ContentValues cv = new ContentValues();
        cv.put("person_name", this.name);
        cv.put("pussy", this.traditional ? 1 : 0);
        cv.put("oral", this.oral ? 1 : 0);
        cv.put("anal", this.anal ? 1 : 0);
        return cv;
    }
}
