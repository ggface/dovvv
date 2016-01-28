package com.ggface.achivetricks;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

/**
 * Created by ggface on 28.05.15.
 */
public class UI {


    public static void gone(View view) {
        if (view != null)
            view.setVisibility(View.GONE);
    }

    public static void hide(View view) {
        if (view != null)
            view.setVisibility(View.INVISIBLE);
    }

    public static void show(View view) {
        if (view != null)
            view.setVisibility(View.VISIBLE);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }

    public static void text(Context context, int resId) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show();
    }

    public static void text(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
