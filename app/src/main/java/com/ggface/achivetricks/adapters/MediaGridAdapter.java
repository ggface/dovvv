package com.ggface.achivetricks.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ggface.achivetricks.App;
import com.ggface.achivetricks.R;
import com.ggface.achivetricks.classes.AnimUtils;
import com.ggface.achivetricks.classes.Person;
import com.ggface.achivetricks.classes.PixelUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MediaGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int mItemWidth, mItemHeight, mMargin, mColumns;
    private List<OverviewItem> mItems = new ArrayList<>();
    //	private ArrayList<Media> mData = new ArrayList<>();
    private MediaGridAdapter.OnItemClickListener mItemClickListener;
    final int NORMAL = 0, LOADING = 1;

    public MediaGridAdapter(Context context, List<Person> items, Integer columns) {
        mColumns = columns;

        int screenWidth = PixelUtils.getScreenWidth(context);
        mItemWidth = (screenWidth / columns);
        mItemHeight = (int) ((double) mItemWidth / 0.677);
        mMargin = PixelUtils.getPixelsFromDp(context, 2);

        setItems(items);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case LOADING:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_griditem_loading, parent, false);
                return new MediaGridAdapter.LoadingHolder(v);
            case NORMAL:
            default:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_griditem, parent, false);
                return new MediaGridAdapter.ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        int double_margin = mMargin * 2;
        int top_margin = (position < mColumns) ? mMargin * 2 : mMargin;

        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
        layoutParams.height = mItemHeight;
        layoutParams.width = mItemWidth;
        int mod = 0;
        if (position % mColumns == mod) {
            layoutParams.setMargins(double_margin, top_margin, mMargin, mMargin);
        } else if (position % mColumns == mColumns - 1) {
            layoutParams.setMargins(mMargin, top_margin, double_margin, mMargin);
        } else {
            layoutParams.setMargins(mMargin, top_margin, mMargin, mMargin);
        }
        viewHolder.itemView.setLayoutParams(layoutParams);

        if (getItemViewType(position) == NORMAL) {
            final ViewHolder videoViewHolder = (ViewHolder) viewHolder;
            final OverviewItem overviewItem = getItem(position);
            Person item = overviewItem.person;


            videoViewHolder.title.setText(item.name);

            String sd = item.traditional ? "v" : "x";
            String so = item.oral ? "v" : "x";
            String sa = item.anal ? "v" : "x";
            videoViewHolder.year.setText(sd + so + sa);

            videoViewHolder.coverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            videoViewHolder.coverImage.setVisibility(View.GONE);
            videoViewHolder.title.setVisibility(View.GONE);
            videoViewHolder.year.setVisibility(View.GONE);

            File file = null;
            if (item.getFilename() != null)
                file = App.getContext().getFileStreamPath(item.getFilename());

            RequestCreator rc;
            if (file != null) {
                rc = Picasso.with(videoViewHolder.coverImage.getContext()).load(file);
            } else
                rc = Picasso.with(videoViewHolder.coverImage.getContext()).load(R.drawable.test_photo_portret);

            rc.resize(mItemWidth, mItemHeight)
                    .centerCrop()
                    .transform(DrawGradient.INSTANCE)
                    .into(videoViewHolder.coverImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            overviewItem.isImageError = false;
                            AnimUtils.fadeIn(videoViewHolder.coverImage);
                            AnimUtils.fadeIn(videoViewHolder.title);
                            AnimUtils.fadeIn(videoViewHolder.year);
                        }

                        @Override
                        public void onError() {
                            overviewItem.isImageError = true;
                            AnimUtils.fadeIn(videoViewHolder.title);
                            AnimUtils.fadeIn(videoViewHolder.year);
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position).isLoadingItem) {
            return LOADING;
        }
        return NORMAL;
    }

    public OverviewItem getItem(int position) {
        if (position < 0 || mItems.size() <= position) return null;
        return mItems.get(position);
    }

    public void setOnItemClickListener(MediaGridAdapter.OnItemClickListener listener) {
        mItemClickListener = listener;
    }


    public void removeLoading() {
        if (getItemCount() <= 0) return;
        OverviewItem item = mItems.get(getItemCount() - 1);
        if (item.isLoadingItem) {
            mItems.remove(getItemCount() - 1);
            notifyDataSetChanged();
        }
    }


    public void addLoading() {
        OverviewItem item = null;
        if (getItemCount() != 0) {
            item = mItems.get(getItemCount() - 1);
        }

        if (getItemCount() == 0 || (item != null && !item.isLoadingItem)) {
            mItems.add(new OverviewItem(true));
            notifyDataSetChanged();
        }
    }


    public boolean isLoading() {
        if (getItemCount() <= 0) return false;
        return getItemViewType(getItemCount() - 1) == LOADING;
    }


    public void setItems(List<Person> items) {
        // Clear items
        mItems.clear();
        // Add new items, if available
        if (null != items) {
            for (Person item : items) {
                mItems.add(new OverviewItem(item));
            }
        }
        notifyDataSetChanged();
    }

    public void clearItems() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        public void onItemClick(View v, Person item, int position);
    }

    public List<Person> getItems() {
        List<Person> exportSource = null;
        if (null != mItems && mItems.size() > 0) {
            exportSource = new ArrayList<>(mItems.size());
            for (OverviewItem item : mItems) {
                exportSource.add(item.person);
            }
        }
        return exportSource;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        View itemView;
        @Bind(R.id.focus_overlay)
        View focusOverlay;
        @Bind(R.id.cover_image)
        ImageView coverImage;
        @Bind(R.id.title)
        TextView title;
        @Bind(R.id.year)
        TextView year;

        private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusOverlay.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        };

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.itemView = itemView;
            itemView.setOnClickListener(this);
            coverImage.setMinimumHeight(mItemHeight);

            itemView.setOnFocusChangeListener(mOnFocusChangeListener);
        }

        public ImageView getCoverImage() {
            return coverImage;
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                int position = getPosition();
                Person item = getItem(position).person;
                mItemClickListener.onItemClick(view, item, position);
            }
        }

    }

    class LoadingHolder extends RecyclerView.ViewHolder {

        View itemView;

        public LoadingHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemView.setMinimumHeight(mItemHeight);
        }

    }

    class OverviewItem {
        Person person;
        boolean isImageError = true;
        boolean isLoadingItem = false;

        OverviewItem(Person media) {
            this.person = media;
        }

        OverviewItem(boolean loading) {
            this.isLoadingItem = loading;
        }
    }

    private static class DrawGradient implements Transformation {
        static Transformation INSTANCE = new DrawGradient();

        @Override
        public Bitmap transform(Bitmap src) {
            // Code borrowed from https://stackoverflow.com/questions/23657811/how-to-mask-bitmap-with-lineargradient-shader-properly
            int w = src.getWidth();
            int h = src.getHeight();
            Bitmap overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(overlay);

            canvas.drawBitmap(src, 0, 0, null);
            src.recycle();

            Paint paint = new Paint();
            float gradientHeight = h / 2f;
            LinearGradient shader = new LinearGradient(0, h - gradientHeight, 0, h, 0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawRect(0, h - gradientHeight, w, h, paint);
            return overlay;
        }

        @Override
        public String key() {
            return "gradient()";
        }
    }
}
