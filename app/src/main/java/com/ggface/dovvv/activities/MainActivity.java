package com.ggface.dovvv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.ggface.dovvv.BuildConfig;
import com.ggface.dovvv.R;
import com.ggface.dovvv.UI;
import com.ggface.dovvv.Units;
import com.ggface.dovvv.adapters.MediaGridAdapter;
import com.ggface.dovvv.classes.DBHelper;
import com.ggface.dovvv.classes.IOUtils;
import com.ggface.dovvv.classes.Person;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private MediaGridAdapter mAdapter;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.persons_recycler_view)
    RecyclerView mPersonsRecyclerView;

    //region Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Integer mColumns = getResources().getInteger(R.integer.overview_cols);

        initToolbar();
        initAdapter(mColumns);
        initRecyclerView(mColumns);
        IOUtils.clearOutdatedFiles(this, mAdapter.getItems());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Person> items = DBHelper.getInstance(this).read();
        mAdapter.setItems(items);
    }
    //endregion Lifecycle

    @OnClick(R.id.fab)
    void onAddPersonClick() {
        PersonActivity.startActivity(this, Units.VAR_NEW_PERSON);
    }

    private void initToolbar() {
        mToolbar.setLogo(R.mipmap.ic_launcher);
        if (BuildConfig.DEBUG) {
            mToolbar.inflateMenu(R.menu.menu_main);
            mToolbar.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.action_export:
                        IOUtils.exportData(this, mAdapter.getItems());
                        UI.text(MainActivity.this, getString(R.string.export_complete));
                        return true;
                    case R.id.action_import:
                        List<Person> backupList = IOUtils.getBackupList(this);
                        mAdapter.setItems(backupList);
                        UI.text(MainActivity.this, getString(R.string.backup_list_made));
                        return true;
                }
                return false;
            });
        }
    }

    private void initAdapter(int columns) {
        List<Person> mItems = DBHelper.getInstance(this).read();

        mAdapter = new MediaGridAdapter(this, mItems, columns);
        mAdapter.setOnItemClickListener(person -> {
            if (Units.VAR_NEW_PERSON < person.id) {
                PersonActivity.startActivity(this, person.id);
            } else {
                IOUtils.importData(this, person.fullpath);
                UI.text(MainActivity.this, getString(R.string.data_wrote_to_database));
            }
        });
    }

    private void initRecyclerView(int columns) {
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, columns);
        mPersonsRecyclerView.setLayoutManager(mLayoutManager);
        mPersonsRecyclerView.setHasFixedSize(true);
        mPersonsRecyclerView.setAdapter(mAdapter);
    }
}