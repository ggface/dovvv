package com.ggface.achivetricks.classes;

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
}
