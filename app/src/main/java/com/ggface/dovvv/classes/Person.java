package com.ggface.dovvv.classes;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.ggface.dovvv.Units;
import com.google.gson.annotations.Expose;

/**
 * @author Ivan Novikov on
 */
public class Person implements Parcelable {

    public static final Creator<Person> CREATOR = new ClassCreator();

    @Expose
    public long id;

    @Expose
    public int mPosition;

    @Expose
    public String extension;

    @Expose
    public String name;

    @Expose
    public boolean oral, anal, traditional;

    public String fullpath;

    protected Person(Parcel parcel) {
        id = parcel.readLong();
        mPosition = parcel.readInt();
        extension = parcel.readString();
        name = parcel.readString();
        oral = PojoUtils.toBoolean(parcel.readByte());
        anal = PojoUtils.toBoolean(parcel.readByte());
        traditional = PojoUtils.toBoolean(parcel.readByte());
        fullpath = parcel.readString();
    }

    public Person() {
    }

    public String getFilename() {
        if (id <= Units.VAR_NEW_PERSON || null == extension) {
            return null;
        }
        return "dovvv_photo_" + String.valueOf(id) + "." + extension;
    }

    public ContentValues toDB() {
        ContentValues cv = new ContentValues();
        cv.put("person_name", this.name);
        cv.put("pussy", this.traditional ? 1 : 0);
        cv.put("oral", this.oral ? 1 : 0);
        cv.put("anal", this.anal ? 1 : 0);
        cv.put("ext", this.extension);
        cv.put("position", mPosition);
        return cv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Person that = (Person) o;
        return PojoUtils.equal(id, that.id) &&
                PojoUtils.equal(mPosition, that.mPosition) &&
                PojoUtils.equal(extension, that.extension) &&
                PojoUtils.equal(name, that.name) &&
                PojoUtils.equal(oral, that.oral) &&
                PojoUtils.equal(anal, that.anal) &&
                PojoUtils.equal(traditional, that.traditional) &&
                PojoUtils.equal(fullpath, that.fullpath);
    }

    @Override
    public int hashCode() {
        return PojoUtils.hashCode(id, mPosition, extension, name, oral, anal,
                traditional, fullpath);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", mPosition='" + mPosition + '\'' +
                ", extension='" + extension + '\'' +
                ", name='" + name + '\'' +
                ", oral=" + oral +
                ", anal=" + anal +
                ", traditional=" + traditional +
                ", fullpath='" + fullpath + '\'' +
                '}';
    }

    //region Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(mPosition);
        dest.writeString(extension);
        dest.writeString(name);
        dest.writeByte(PojoUtils.toByte(oral));
        dest.writeByte(PojoUtils.toByte(anal));
        dest.writeByte(PojoUtils.toByte(traditional));
        dest.writeString(fullpath);
    }
    //endregion Parcelable

    private static final class ClassCreator implements Creator<Person> {
        @Override
        public Person createFromParcel(Parcel parcel) {
            return new Person(parcel);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    }
}
