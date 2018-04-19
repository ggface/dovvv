package com.ggface.dovvv.adapters;

import android.support.annotation.NonNull;

import com.ggface.dovvv.classes.Person;

/**
 * @author Ivan Novikov on 2016-05-12
 */
public interface OnPersonItemClickListener {

    /**
     * Perform click by person
     *
     * @param person person element
     */
    void onPersonClick(@NonNull Person person);
}