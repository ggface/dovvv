package com.ggface.dovvv.classes;

import android.content.ContentValues;
import android.graphics.Color;

import com.ggface.dovvv.Units;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class Person implements Serializable {

    @Expose
    public long id;

    @Expose
    public String extension;

    @Expose
    public String name;

    @Expose
    public boolean oral, anal, traditional;

    public int color;
    public String fullpath;

    public static Person clone(Person instance) {
        Person person = new Person();
        person.id = instance.id;
        person.name = instance.name;

        person.traditional = instance.traditional;
        person.oral = instance.oral;
        person.anal = instance.anal;

        person.extension = instance.extension;
        person.fullpath = instance.fullpath;
        return person;
    }

    public Person() {
        this.color = Color.parseColor("#3F51B5");
    }

    public Person(String name) {
        this();
        this.name = name;
//        this.traditional = true;
//        this.oral = true;
//        this.anal = true;
    }

    public String getFilename() {
        if (id <= Units.VAR_NEW_PERSON || null == extension)
            return null;
        return "dovvv_photo_" + String.valueOf(id) + "." + extension;
    }

    public ContentValues toDB() {
        ContentValues cv = new ContentValues();
        cv.put("person_name", this.name);
        cv.put("pussy", this.traditional ? 1 : 0);
        cv.put("oral", this.oral ? 1 : 0);
        cv.put("anal", this.anal ? 1 : 0);
        cv.put("ext", this.extension);

//        if (image != null) {
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            image.compress(Bitmap.CompressFormat.PNG, 0, stream);
//            cv.put("girl_photo", stream.toByteArray());
//        }
        return cv;
    }

    public String toJson() {
        return new Gson().toJson(this, Person.class);
    }

}
