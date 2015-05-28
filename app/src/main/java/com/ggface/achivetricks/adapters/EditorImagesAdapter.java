package com.ggface.achivetricks.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.ggface.achivetricks.R;
import com.ggface.achivetricks.UI;
import com.ggface.achivetricks.classes.EditorBodyImage;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ggface on 20.04.15.
 */
public class EditorImagesAdapter extends BaseAdapter {

    private int mItemHeight = 0;
    private int mNumColumns = 0;
    private RelativeLayout.LayoutParams mImageViewLP;

    private Context mContext;
    private List<EditorBodyImage> items;
    private LayoutInflater inflater;

    public EditorImagesAdapter(Context c) {
        this.mContext = c;
        this.inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageViewLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        this.items = new ArrayList<>();

        EditorBodyImage plus = new EditorBodyImage();
        this.items.add(plus);
    }

    public int getCount() {
        return items == null ? 0 : items.size();
    }

    public EditorBodyImage getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gi_editor_image, parent, false);
        }

        ImageView ivThumb = UI.get(convertView, R.id.ivThumb);
        ProgressBar progress = UI.get(convertView, R.id.progress);
        EditorBodyImage item = getItem(position);

        UI.gone(progress);
        UI.show(ivThumb);

        ivThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivThumb.setLayoutParams(mImageViewLP);

        if (ivThumb.getLayoutParams().height != mItemHeight) {
            ivThumb.setLayoutParams(mImageViewLP);
        }


//        Picasso.with(mContext)
//                .load(R.drawable.ic_template_nophoto)
//                .into(ivThumb);

        return convertView;
    }

    public List<EditorBodyImage> getItems() {
        return this.items;
    }

    // set numcols
    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }

    public int getNumColumns() {
        return mNumColumns;
    }

    // set photo item height
    public void setItemHeight(int height) {
        if (height == mItemHeight) {
            return;
        }
        mItemHeight = height;
        mImageViewLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, mItemHeight);
        notifyDataSetChanged();
    }

    public void rewrite(List<EditorBodyImage> value) {
        this.items.clear();
        EditorBodyImage plus = new EditorBodyImage();
        this.items.add(plus);
        this.items.addAll(value);
        notifyDataSetChanged();
    }
}
