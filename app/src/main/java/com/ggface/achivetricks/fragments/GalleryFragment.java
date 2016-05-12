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

import butterknife.Bind;
import butterknife.ButterKnife;

public class GalleryFragment extends Fragment {

    private final View.OnClickListener doneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), PersonActivity.class);
            startActivityForResult(intent, RequestCodes.RC_PERSON);
        }
    };

    private MediaGridAdapter.OnItemClickListener mOnItemClickListener = new MediaGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(final View view, final Person item, final int position) {
            Intent intent = new Intent(getActivity(), PersonActivity.class);
            intent.putExtra(Units.ARG_INDEX, item.id);
            startActivityForResult(intent, RequestCodes.RC_PERSON);
        }
    };

    private MediaGridAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private Integer mColumns = 2;

    @Bind(R.id.fab)
    FloatingActionButton fab;

    @Bind(R.id.rvCollection)
    RecyclerView rvCollection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mColumns = getResources().getInteger(R.integer.overview_cols);
        List<Person> mItems = DBHelper.getInstance(getActivity()).read();
        mAdapter = new MediaGridAdapter(getActivity(), mItems, mColumns);
        mAdapter.setOnItemClickListener(mOnItemClickListener);
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
        ButterKnife.bind(this, view);

        mLayoutManager = new GridLayoutManager(getActivity(), mColumns);

        rvCollection.setLayoutManager(mLayoutManager);
        rvCollection.setHasFixedSize(true);
        rvCollection.setAdapter(mAdapter);

        fab.setOnClickListener(doneClickListener);
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
            UI.text(getActivity(), getString(R.string.export_complete));
            return true;
        }

        return super.onOptionsItemSelected(item);
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
}
