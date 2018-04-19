package com.ggface.dovvv.classes;

import java.util.List;

public interface IRoom {

    List<Person> read();

    Person select(long id);

    long insert(Person instance);

    void update(Person instance);

    void remove(Person instance);
}