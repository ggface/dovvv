package com.ggface.achivetricks.fragments;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

import com.ggface.achivetricks.R;
import com.ggface.achivetricks.UI;
import com.ggface.achivetricks.adapters.EditorImagesAdapter;
import com.ggface.achivetricks.classes.EditorBodyImage;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class GalleryFragment extends Fragment {

    private GridView gvCollection;
    private EditorImagesAdapter adapter;
    private int mPhotoSize, mPhotoSpacing;
    private List<EditorBodyImage> bodyViews;

    public GalleryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = new EditorImagesAdapter(getActivity());

        bodyViews = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPhotoSize = getResources().getDimensionPixelSize(R.dimen.photo_size);
        mPhotoSpacing = getResources().getDimensionPixelSize(R.dimen.photo_spacing);

        gvCollection = UI.get(view, R.id.gvCollection);
        gvCollection.setAdapter(adapter);

        gvCollection.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (adapter.getNumColumns() == 0) {
                    final int numColumns = (int) Math.floor(gvCollection.getWidth() / (mPhotoSize + mPhotoSpacing));
                    if (numColumns > 0) {
                        final int columnWidth = (gvCollection.getWidth() / numColumns) - mPhotoSpacing;
                        adapter.setNumColumns(numColumns);
                        adapter.setItemHeight(columnWidth);

                    }
                }
            }
        });

        gvCollection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if (position == 0) {
//                    Intent intent = new Intent(Intent.ACTION_PICK,
//                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    startActivityForResult(intent, RequestCodes.RC_GALLERY);
//                } else {
//                    PopupMenu popupMenu = new PopupMenu(getActivity(), view);
//                    popupMenu.getMenuInflater().inflate(R.menu.popup_menu_editor_gallary
//                            , popupMenu.getMenu());
//
//                    popupMenu.getMenu().findItem(R.id.action_upload)
//                            .setVisible(adapter.getItem(position).isLocal());
//
//                    popupMenu.setOnMenuItemClickListener(new OnPopupItemClickListener(position));
//
//                    popupMenu.show();
//
//                }
            }
        });
    }
}
