package com.ggface.dovvv.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ggface.dovvv.R;
import com.ggface.dovvv.Units;
import com.ggface.dovvv.classes.AnimUtils;
import com.ggface.dovvv.classes.Person;
import com.ggface.dovvv.classes.PixelUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int mItemWidth, mItemHeight, mMargin, mColumns;
    private List<Person> mItems = new ArrayList<>();
    private OnPersonItemClickListener mItemClickListener;
    private ScaleToFitWidthHeightTransform transform;

    public MediaGridAdapter(Context context, List<Person> items, Integer columns) {
        mColumns = columns;
        int screenWidth = PixelUtils.getScreenWidth(context);
        mItemWidth = (screenWidth / columns);
        mItemHeight = (int) ((double) mItemWidth / 0.677);
        mMargin = PixelUtils.getPixelsFromDp(context, 2);
        transform = new ScaleToFitWidthHeightTransform(mItemHeight, false);
        setItems(items);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_griditem, parent, false);
        return new MediaGridAdapter.ViewHolder(v);
    }

    @SuppressWarnings("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {
        int double_margin = mMargin * 2;
        int top_margin = (position < mColumns) ? mMargin * 2 : mMargin;

        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        int mod = 0;
        int side;
        if (position % mColumns == mod) {
            layoutParams.setMargins(double_margin, top_margin, mMargin, mMargin);
            side = double_margin + mMargin;
        } else if (position % mColumns == mColumns - 1) {
            layoutParams.setMargins(mMargin, top_margin, double_margin, mMargin);
            side = mMargin + double_margin;
        } else {
            layoutParams.setMargins(mMargin, top_margin, mMargin, mMargin);
            side = mMargin + mMargin;
        }
        layoutParams.height = mItemHeight;
        layoutParams.width = mItemWidth - side;
        viewHolder.itemView.setLayoutParams(layoutParams);

        final ViewHolder videoViewHolder = (ViewHolder) viewHolder;
        final Person person = mItems.get(position);

        videoViewHolder.name.setText(person.name);

        if (person.id > Units.VAR_NEW_PERSON) {
            String sd = person.traditional ? "v" : "x";
            String so = person.oral ? "v" : "x";
            String sa = person.anal ? "v" : "x";
            videoViewHolder.markers.setText(String.format("%s%s%s", sd, so, sa));
        } else {
            videoViewHolder.markers.setText(person.extension);
        }

        videoViewHolder.coverImage.setVisibility(View.GONE);
        videoViewHolder.name.setVisibility(View.GONE);
        videoViewHolder.markers.setVisibility(View.GONE);

        File file = null;
        if (person.getFilename() != null) {
            file = new File(viewHolder.itemView.getContext().getFilesDir().getAbsolutePath(), person.getFilename());
        }

        RequestCreator rc;
        if (file != null && file.exists() && file.canRead()) {
            rc = Picasso.with(videoViewHolder.coverImage.getContext()).load(file);
        } else {
            rc = Picasso.with(videoViewHolder.coverImage.getContext()).load(R.drawable.test_photo_portret);
        }


        rc.transform(transform)
                .into(videoViewHolder.coverImage
                        , new Callback() {
                            @Override
                            public void onSuccess() {
                                AnimUtils.fadeIn(videoViewHolder.coverImage);
                                AnimUtils.fadeIn(videoViewHolder.name);
                                AnimUtils.fadeIn(videoViewHolder.markers);
                                videoViewHolder.placeholder_image.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError() {
                                AnimUtils.fadeIn(videoViewHolder.name);
                                AnimUtils.fadeIn(videoViewHolder.markers);
                            }
                        }
                );
    }

    @Override
    public int getItemCount() {
        return null == mItems ? 0 : mItems.size();
    }

    public void setOnItemClickListener(OnPersonItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setItems(List<Person> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    public List<Person> getItems() {
        return mItems;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View focusOverlay;
        ImageView coverImage, placeholder_image;
        TextView name;
        TextView markers;

        private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusOverlay.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        };

        ViewHolder(View view) {
            super(view);
            focusOverlay = view.findViewById(R.id.focus_overlay);
            coverImage = view.findViewById(R.id.cover_image);
            placeholder_image = view.findViewById(R.id.placeholder_image);
            name = view.findViewById(R.id.name);
            markers = view.findViewById(R.id.markers);

            itemView.setOnClickListener(this);
            coverImage.setMinimumHeight(mItemHeight);

            itemView.setOnFocusChangeListener(mOnFocusChangeListener);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                int position = getAdapterPosition();
                Person item = mItems.get(position);
                mItemClickListener.onPersonClick(item);
            }
        }
    }

    public class ScaleToFitWidthHeightTransform implements Transformation {

        private int mSize;
        private boolean isHeightScale;

        ScaleToFitWidthHeightTransform(int size, boolean isHeightScale) {
            mSize = size;
            this.isHeightScale = isHeightScale;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            float scale;
            int newSize;
            Bitmap scaleBitmap;
            if (isHeightScale) {
                scale = (float) mSize / source.getHeight();
                newSize = Math.round(source.getWidth() * scale);
                scaleBitmap = Bitmap.createScaledBitmap(source, newSize, mSize, true);
            } else {
                scale = (float) mSize / source.getWidth();
                newSize = Math.round(source.getHeight() * scale);
                scaleBitmap = Bitmap.createScaledBitmap(source, mSize, newSize, true);
            }

            if (scaleBitmap != source) {
                source.recycle();
            }
            return scaleBitmap;
        }

        @Override
        public String key() {
            return "scaleRespectRatio" + mSize + isHeightScale;
        }
    }
}
