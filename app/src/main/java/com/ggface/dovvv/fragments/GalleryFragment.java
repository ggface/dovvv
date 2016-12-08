package com.ggface.dovvv.fragments;

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

import com.ggface.dovvv.App;
import com.ggface.dovvv.R;
import com.ggface.dovvv.UI;
import com.ggface.dovvv.Units;
import com.ggface.dovvv.activities.PersonActivity;
import com.ggface.dovvv.adapters.MediaGridAdapter;
import com.ggface.dovvv.classes.DBHelper;
import com.ggface.dovvv.classes.IRoom;
import com.ggface.dovvv.classes.Person;
import com.ggface.dovvv.classes.Tools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GalleryFragment extends Fragment {

    private final View.OnClickListener doneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), PersonActivity.class);
            startActivityForResult(intent, Units.RC_PERSON);
        }
    };

    private MediaGridAdapter.OnItemClickListener mOnItemClickListener = new MediaGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(final View view, final Person item, final int position) {

            if (Units.VAR_NEW_PERSON < item.id) {
                Intent intent = new Intent(getActivity(), PersonActivity.class);
                intent.putExtra(Units.ARG_INDEX, item.id);
                startActivityForResult(intent, Units.RC_PERSON);
            } else {
                importData(item.fullpath);
                UI.text(getActivity(), getString(R.string.data_wrote_to_database));
            }
        }
    };

    private MediaGridAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private Integer mColumns = 2;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.rvCollection)
    RecyclerView rvCollection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mColumns = getResources().getInteger(R.integer.overview_cols);
        List<Person> mItems = getRoom().read();

//        List<Person> mItems = new ArrayList<>();
//        mItems.add(new Person("Sophia"));
//        mItems.add(new Person("Emma"));
//        mItems.add(new Person("Olivia"));
//        mItems.add(new Person("Ava"));
//        mItems.add(new Person("Isabella"));
//        mItems.add(new Person("Mia"));
//        mItems.add(new Person("Zoe"));
//        mItems.add(new Person("Lily"));
//        mItems.add(new Person("Emily"));
//        mItems.add(new Person("Madelyn"));
//        mItems.add(new Person("Madison"));
//        mItems.add(new Person("Chloe"));
//        mItems.add(new Person("Charlotte"));
//        mItems.add(new Person("Aubrey"));

        mAdapter = new MediaGridAdapter(getActivity(), mItems, mColumns);
        mAdapter.setOnItemClickListener(mOnItemClickListener);

        removeTrash();
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
        List<Person> items = getRoom().read();
        mAdapter.setItems(items);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_export) {
            exportData();
            UI.text(getActivity(), getString(R.string.export_complete));
            return true;
        } else if (id == R.id.action_import) {
            makeBackupList();
            UI.text(getActivity(), getString(R.string.backup_list_made));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private IRoom getRoom() {
        return DBHelper.getInstance(getActivity());
    }

    private void removeTrash() {
        List<File> existsPhotos = new ArrayList<>();

        if (null != mAdapter.getItems())
            for (Person person : mAdapter.getItems()) {
                if (null != person.getFilename()) {
                    File src = App.getContext().getFileStreamPath(person.getFilename());
                    existsPhotos.add(src);
                }
            }

        List<File> allFiles = Arrays.asList(App.getContext().getFilesDir().listFiles());

        if (null != allFiles) {
            Iterator<File> it = allFiles.iterator();
            while (it.hasNext()) {
                File file = it.next();
                String filename = file.getName();
                if (filename.contains("dovvv_photo_")) {
                    if (!existsPhotos.contains(file))
                        file.delete();
                }
            }
        }
        UI.text(getActivity(), "Welcome");
    }

    private void exportData() {
        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .create();

            List<Person> persons = this.mAdapter.getItems();
            String json = gson.toJson(persons,
                    new TypeToken<List<Person>>() {
                    }.getType());

            File folder = new File(App.getPIO(null), Tools.getCustomDateFormat(new Date(), "yyyy-MM-dd HHmmss") + " backup");
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

    private void makeBackupList() {
        List<Person> items = new ArrayList<>();
        File[] files = App.getPIO(null).listFiles();
        int iterator = -1;
        for (File inFile : files) {
            if (inFile.isDirectory()) {
                Person p = new Person();
                p.id = iterator--;
                p.fullpath = inFile.getAbsolutePath();
                p.name = inFile.getName();
                p.extension = String.valueOf((int) Tools.folderSize(inFile) / 1024 / 1024) + " mB";
                items.add(p);
            }
        }
        mAdapter.setItems(items);
    }

    private void importData(String srcPath) {
        try {
            DBHelper.getInstance(getActivity()).clear();

            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation().create();

            File dir = new File(srcPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dir + File.separator + "backup.json")));

            List<Person> persons = gson.fromJson(br,
                    new TypeToken<List<Person>>() {
                    }.getType());

            for (Person p : persons) {
                if (p.extension != null) {
                    File photo = new File(dir + File.separator + "dovvv_photo_" + p.id + '.' + p.extension);
                    if (photo.exists()) {
                        Tools.writePhoto(p.id, photo.getAbsoluteFile());
                    }
                }
                getRoom().insert(p);
            }
            mAdapter.setItems(persons);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
