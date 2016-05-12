package com.ggface.achivetricks.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ggface.achivetricks.App;
import com.ggface.achivetricks.R;
import com.ggface.achivetricks.UI;
import com.ggface.achivetricks.classes.Person;
import com.squareup.picasso.Callback;
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
    private List<Person> items;
    private LayoutInflater inflater;

    public EditorImagesAdapter(Context c) {
        this.mContext = c;
        this.inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageViewLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        this.items = new ArrayList<>();

//        EditorBodyImage plus = new EditorBodyImage();
//        this.items.add(plus);
    }

    public int getCount() {
        return items == null ? 0 : items.size();
    }

    public Person getItem(int position) {
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
        final ProgressBar progress = UI.get(convertView, R.id.progress);
        TextView tvName = UI.get(convertView, R.id.tvName);
        Person item = getItem(position);

        UI.show(ivThumb);

        tvName.setText(item.name);

        ivThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivThumb.setLayoutParams(mImageViewLP);

        if (ivThumb.getLayoutParams().height != mItemHeight) {
            ivThumb.setLayoutParams(mImageViewLP);
        }

        if (null != item.getFilename()) {
            UI.show(progress);
            File file = App.getContext().getFileStreamPath(item.getFilename());
            if (!file.exists())
                App.logD("EditorImagesAdapter getView()", file.getAbsolutePath() + " not found ((((");
            else
                App.logD("EditorImagesAdapter getView()", file.getAbsolutePath());
            Picasso.with(mContext)
                    .load(file)
                    .into(ivThumb, new Callback() {
                        @Override
                        public void onSuccess() {
                            UI.gone(progress);
                        }

                        @Override
                        public void onError() {
                            UI.gone(progress);
                        }
                    });
        }
        return convertView;
    }

    public List<Person> getItems() {
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
        mImageViewLP = new RelativeLayout.LayoutParams(mItemHeight, mItemHeight);
        notifyDataSetChanged();
    }

    public void rewrite(List<Person> value) {
        this.items.clear();
        this.items.addAll(value);
        notifyDataSetChanged();
    }
}
