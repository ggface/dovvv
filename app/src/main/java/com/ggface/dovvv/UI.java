package com.ggface.dovvv;

import android.content.Context;
import android.widget.Toast;

public class UI {

    public static void text(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
    }

    public static void text(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}