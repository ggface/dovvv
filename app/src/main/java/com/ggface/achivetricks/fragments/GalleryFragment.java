package com.ggface.achivetricks.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ggface.achivetricks.App;
import com.ggface.achivetricks.R;
import com.ggface.achivetricks.UI;
import com.ggface.achivetricks.Units;
import com.ggface.achivetricks.activities.PersonActivity;
import com.ggface.achivetricks.adapters.MediaGridAdapter;
import com.ggface.achivetricks.classes.DBHelper;
import com.ggface.achivetricks.classes.Person;
import com.ggface.achivetricks.classes.RequestCodes;
import com.ggface.achivetricks.classes.Tools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class GalleryFragment extends Fragment {

    private enum State {
        UNINITIALISED, LOADING, SEARCHING, LOADING_PAGE, LOADED, LOADING_DETAIL
    }

    private final View.OnClickListener doneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), PersonActivity.class);
//            intent.putExtra(Units.BARCODE, mCode);
//            intent.putExtra(Units.IMAGE_URL, imageUrl);
            startActivityForResult(intent, RequestCodes.RC_PERSON);
        }
    };

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            mVisibleItemCount = mLayoutManager.getChildCount();
            mTotalItemCount = mLayoutManager.getItemCount() - (mAdapter.isLoading() ? 1 : 0);
            mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

            if (mState == State.LOADING_PAGE) {
                if (mTotalItemCount > mPreviousTotal) {
                    mPreviousTotal = mTotalItemCount;
                    mPreviousTotal = mTotalItemCount = mLayoutManager.getItemCount();
                    setState(State.LOADED);
                }
            }

//            if (!mEndOfListReached && !(mState == State.SEARCHING) && !(mState == State.LOADING_PAGE) && !(mState == State.LOADING) && (mTotalItemCount - mVisibleItemCount) <= (mFirstVisibleItem +
//                    mLoadingTreshold)) {
//
//                mFilters.page = mPage;
//                mCurrentCall = mProvider.getList(mItems, new MediaProvider.Filters(mFilters), mCallback);
//
//                mPreviousTotal = mTotalItemCount = mLayoutManager.getItemCount();
//                setState(State.LOADING_PAGE);
//            }
        }
    };

    private MediaGridAdapter.OnItemClickListener mOnItemClickListener = new MediaGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(final View view, final Person item, final int position) {
            Intent intent = new Intent(getActivity(), PersonActivity.class);
            intent.putExtra(Units.ARG_INDEX, item.id);
            startActivityForResult(intent, RequestCodes.RC_PERSON);
            /**
             * We shouldn't really be doing the palette loading here without any ui feedback,
             * but it should be really quick
             */
//            RecyclerView.ViewHolder holder = rvCollection.getChildViewHolder(view);
//            if (holder instanceof MediaGridAdapter.ViewHolder) {
//                ImageView coverImage = ((MediaGridAdapter.ViewHolder) holder).getCoverImage();
//
//                if (coverImage.getDrawable() == null) {
//                    showLoadingDialog(position);
//                    return;
//                }
//
//                Bitmap cover = ((BitmapDrawable) coverImage.getDrawable()).getBitmap();
//                Palette.generateAsync(cover, 5, new Palette.PaletteAsyncListener() {
//                    @Override
//                    public void onGenerated(Palette palette) {
//                        int vibrantColor = palette.getVibrantColor(-1);
//                        int paletteColor;
//                        if (vibrantColor == -1) {
//                            paletteColor = palette.getMutedColor(getResources().getColor(R.color.primary));
//                        } else {
//                            paletteColor = vibrantColor;
//                        }
//                        item.color = paletteColor;
//                        showLoadingDialog(position);
//                    }
//                });
//            } else {
//                showLoadingDialog(position);
//            }

        }
    };

    private RecyclerView rvCollection;
    //    private GridView gvCollection;
//    private EditorImagesAdapter adapter;
//    private int mPhotoSize, mPhotoSpacing;
//    private List<EditorBodyImage> bodyViews;
    private MediaGridAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private Integer mColumns = 2, mRetries = 0;
    private State mState = State.UNINITIALISED;

    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount = 0, mLoadingTreshold = mColumns * 3, mPreviousTotal = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
//        adapter = new EditorImagesAdapter(getActivity());

//        bodyViews = new ArrayList<>();
//        DBHelper.getInstance(getActivity()).reCreate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mPhotoSize = getResources().getDimensionPixelSize(R.dimen.photo_size);
//        mPhotoSpacing = getResources().getDimensionPixelSize(R.dimen.photo_spacing);

        rvCollection = UI.get(view, R.id.rvCollection);
//        gvCollection = UI.get(view, R.id.gvCollection);

        mColumns = getResources().getInteger(R.integer.overview_cols);
        mLayoutManager = new GridLayoutManager(getActivity(), mColumns);
        rvCollection.setLayoutManager(mLayoutManager);

        rvCollection.setHasFixedSize(true);
//        rvCollection.addOnScrollListener(mScrollListener);
        //adapter should only ever be created once on fragment initialise.
        List<Person> mItems = DBHelper.getInstance(getActivity()).read();
        mAdapter = new MediaGridAdapter(getActivity(), mItems, mColumns);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        rvCollection.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(doneClickListener);

//        gvCollection.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                if (adapter.getNumColumns() == 0) {
//                    final int numColumns = (int) Math.floor(gvCollection.getMeasuredWidth() / (mPhotoSize ));
//                    if (numColumns > 0) {
//                        final int columnWidth = (gvCollection.getWidth() / numColumns) ;
//                        gvCollection.setColumnWidth(columnWidth);
//                        adapter.setNumColumns(numColumns);
//                        adapter.setItemHeight(columnWidth);
//                    }
//                }
//            }
//        });

//        gvCollection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(getActivity(), PersonActivity.class);
//                intent.putExtra(Units.ARG_INDEX, adapter.getItem(position).id);
//                startActivityForResult(intent, RequestCodes.RC_PERSON);
//            }
//        });
//
//        List<Person> items = DBHelper.getInstance(getActivity()).read();
//        gvCollection.setAdapter(adapter);
//        adapter.rewrite(items);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Person> items = DBHelper.getInstance(getActivity()).read();
        mAdapter.setItems(items);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_export) {
            exportData();
            UI.text(getActivity(), "Export complete");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLoadingDialog(Integer position) {
        // TODO: 11.05.16
    }

    private void setState(State state) {
        if (mState == state) return;//do nothing
        mState = state;
        updateUI();
    }

    private void exportData() {
        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .create();

            List<Person> persons = this.mAdapter.getItems();
            String json = gson.toJson(persons,
                    new TypeToken<List<Person>>() {
                    }.getType());

            File folder = new File(App.getPIO(), Tools.getCustomDateFormat(new Date(), "yyyy-MM-dd HHmmss") + " backup");
            if (!folder.exists())
                folder.mkdirs();
            File cookiesFile = new File(folder, "backup.json");
            new FileOutputStream(cookiesFile).write(json.getBytes());

            for (Person person : persons) {
                if (null != person.getFilename()) {
                    File src = App.getContext().getFileStreamPath(person.getFilename());
                    Tools.copyFile(src, new File(folder, person.getFilename()));
                }
            }
        } catch (Exception e) {
            UI.text(getActivity(), e.getMessage());
        }
    }

    private void updateUI() {
        if (!isAdded()) return;

//        ThreadUtils.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                //animate recyclerview to full alpha
//                //		if (mRecyclerView.getAlpha() != 1.0f)
//                //			mRecyclerView.animate().alpha(1.0f).setDuration(100).start();
//
//                //update loading message based on state
//                switch (mState) {
//                    case LOADING_DETAIL:
//                        mLoadingMessage = R.string.loading_details;
//                        break;
//                    case SEARCHING:
//                        mLoadingMessage = R.string.searching;
//                        break;
//                    default:
//                        int providerMessage = mProvider.getLoadingMessage();
//                        mLoadingMessage = providerMessage > 0 ? providerMessage : R.string.loading_data;
//                        break;
//                }
//
//                switch (mState) {
//                    case LOADING_DETAIL:
//                    case SEARCHING:
//                    case LOADING:
//                        if (mAdapter.isLoading()) mAdapter.removeLoading();
//                        //show the progress bar
//                        mRecyclerView.setVisibility(View.VISIBLE);
//                        //				mRecyclerView.animate().alpha(0.5f).setDuration(500).start();
//                        mEmptyView.setVisibility(View.GONE);
//                        mProgressOverlay.setVisibility(View.VISIBLE);
//                        break;
//                    case LOADED:
//                        if (mAdapter.isLoading()) mAdapter.removeLoading();
//                        mProgressOverlay.setVisibility(View.GONE);
//                        boolean hasItems = mItems.size() > 0;
//                        //show either the recyclerview or the empty view
//                        mRecyclerView.setVisibility(hasItems ? View.VISIBLE : View.INVISIBLE);
//                        mEmptyView.setVisibility(hasItems ? View.GONE : View.VISIBLE);
//                        break;
//                    case LOADING_PAGE:
//                        //add a loading view to the adapter
//                        if (!mAdapter.isLoading()) mAdapter.addLoading();
//                        mEmptyView.setVisibility(View.GONE);
//                        mRecyclerView.setVisibility(View.VISIBLE);
//                        break;
//                }
//                updateLoadingMessage();
//            }
//        });
    }
}
