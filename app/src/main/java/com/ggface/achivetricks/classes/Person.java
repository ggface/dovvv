package com.ggface.achivetricks.classes;

import android.content.ContentValues;

import com.ggface.achivetricks.Units;

import java.util.Date;

public class Person {

    public long id;
    public String extension, fullpath;
    public String name;
    public boolean oral, anal, traditional;
    public Date addDate;
//    public Bitmap image;

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    public String getFilename() {
        if (id == Units.VAR_NEW_PERSON || null == extension)
            return null;
        return String.valueOf(id) + "." + extension;
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
}
