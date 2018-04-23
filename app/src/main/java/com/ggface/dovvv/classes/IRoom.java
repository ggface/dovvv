package com.ggface.dovvv.classes;

import android.support.annotation.NonNull;

import java.util.List;

public interface IRoom {

    @NonNull
    List<Person> read();

    Person select(long id);

    long insert(Person instance);

    void update(Person instance);

    void remove(Person instance);
}