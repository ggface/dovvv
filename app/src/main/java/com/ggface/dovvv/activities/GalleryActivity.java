package com.ggface.dovvv.activities;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ggface.dovvv.App;
import com.ggface.dovvv.R;
import com.ggface.dovvv.UI;
import com.ggface.dovvv.Units;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class GalleryActivity extends AppCompatActivity {

    private MediaGridAdapter mAdapter;

    private final View.OnClickListener doneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(GalleryActivity.this, PersonActivity.class);
            startActivityForResult(intent, Units.RC_PERSON);
        }
    };

    private MediaGridAdapter.OnItemClickListener mOnItemClickListener = new MediaGridAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, Person item, int position) {
            if (Units.VAR_NEW_PERSON < item.id) {
                Intent intent = new Intent(GalleryActivity.this, PersonActivity.class);
                intent.putExtra(Units.ARG_INDEX, item.id);
                startActivityForResult(intent, Units.RC_PERSON);
            } else {
                importData(item.fullpath);
                UI.text(GalleryActivity.this, getString(R.string.data_wrote_to_database));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar pToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(pToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);

        Integer mColumns = getResources().getInteger(R.integer.overview_cols);
        List<Person> mItems = getRoom().read();

        mAdapter = new MediaGridAdapter(this, mItems, mColumns);
        mAdapter.setOnItemClickListener(mOnItemClickListener);

        removeTrash();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        RecyclerView rvCollection = (RecyclerView) findViewById(R.id.rvCollection);
        GridLayoutManager mLayoutManager = new GridLayoutManager(this, mColumns);

        rvCollection.setLayoutManager(mLayoutManager);
        rvCollection.setHasFixedSize(true);
        rvCollection.setAdapter(mAdapter);

        fab.setOnClickListener(doneClickListener);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Person> items = getRoom().read();
        mAdapter.setItems(items);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (App.isDev()) {
            getMenuInflater().inflate(R.menu.menu_gallery, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_export) {
            exportData();
            UI.text(this, getString(R.string.export_complete));
            return true;
        } else if (id == R.id.action_import) {
            makeBackupList();
            UI.text(this, getString(R.string.backup_list_made));
            return true;
        } else if (id == R.id.action_sync) {
//            AccountManager am = AccountManager.get(this);
//            Bundle options = new Bundle();
//
//            am.getAuthToken(
//                    myAccount_,                     // Account retrieved using getAccountsByType()
//                    "Manage your tasks",            // Auth scope
//                    options,                        // Authenticator-specific options
//                    this,                           // Your activity
//                    new OnTokenAcquired(),          // Callback called when a token is successfully acquired
//                    new Handler(new OnError()));    // Callback called if an error occurs
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private IRoom getRoom() {
        return DBHelper.getInstance(this);
    }

    private void removeTrash() {
        List<File> existsPhotos = new ArrayList<>();

        if (null != mAdapter.getItems())
            for (Person person : mAdapter.getItems()) {
                if (null != person.getFilename()) {
                    File src = getFileStreamPath(person.getFilename());
                    existsPhotos.add(src);
                }
            }

        List<File> allFiles = Arrays.asList(getFilesDir().listFiles());

        if (allFiles.size() > 0) {
            Iterator<File> it = allFiles.iterator();
            //noinspection WhileLoopReplaceableByForEach
            while (it.hasNext()) {
                File file = it.next();
                String filename = file.getName();
                if (filename.contains("dovvv_photo_")) {
                    if (!existsPhotos.contains(file)) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        }
    }

    private void exportData() {
        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .create();

            List<Person> persons = this.mAdapter.getItems();
            String json = gson.toJson(persons,
                    new TypeToken<List<Person>>() {
                    }.getType());

            File folder = new File(App.getPIO(this, null), Tools.getCustomDateFormat(new Date(), "yyyy-MM-dd HHmmss") + " backup");
            if (!folder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                folder.mkdirs();
            }
            File cookiesFile = new File(folder, "backup.json");

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(cookiesFile);
                fos.write(json.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != fos) {
                    //noinspection ThrowFromFinallyBlock
                    fos.close();
                }
            }
            for (Person person : persons) {
                if (null != person.getFilename()) {
                    File src = getFileStreamPath(person.getFilename());
                    Tools.copyFile(src, new File(folder, person.getFilename()));
                }
            }
        } catch (Exception e) {
            UI.text(this, e.getMessage());
        }
    }

    private void makeBackupList() {
        List<Person> items = new ArrayList<>();
        File[] files = App.getPIO(this, null).listFiles();

        if (null == files)
            return;

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
            DBHelper.getInstance(this).clear();

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
                        Tools.writePhoto(this, p.id, photo.getAbsoluteFile());
                    }
                }
                getRoom().insert(p);
            }
            mAdapter.setItems(persons);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            // Get the result of the operation from the AccountManagerFuture.
            Bundle bundle = null;
            try {
                bundle = result.getResult();

                // The token is a named value in the bundle. The name of the value
                // is stored in the constant AccountManager.KEY_AUTHTOKEN.
                String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            }



        }
    }
}