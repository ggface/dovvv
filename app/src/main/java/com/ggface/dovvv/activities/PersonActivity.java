package com.ggface.dovvv.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ggface.dovvv.R;
import com.ggface.dovvv.Units;
import com.ggface.dovvv.classes.DBHelper;
import com.ggface.dovvv.classes.IRoom;
import com.ggface.dovvv.classes.Person;
import com.ggface.dovvv.classes.Tools;
import com.ggface.dovvv.widgets.WarningToast;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.File;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PersonActivity extends AppCompatActivity implements WarningToast.OnToastListener {

    private Toast wToast;
    private Person mPerson;

    private EditText etName;
    private ImageView ivPhoto;
    private FloatingActionMenu fMenu;

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cbDefault:
                    mPerson.traditional = ((CheckBox) v).isChecked();
                    break;
                case R.id.cbAnal:
                    mPerson.anal = ((CheckBox) v).isChecked();
                    break;
                case R.id.cbOral:
                    mPerson.oral = ((CheckBox) v).isChecked();
                    break;
                case R.id.menu_done:
                    mPerson.name = etName.getText().toString();

                    if (Units.VAR_NEW_PERSON == mPerson.id) {
                        mPerson.id = getRoom().insert(mPerson);
                    }

                    if (Units.VAR_NEW_PERSON == mPerson.id) {
                        showWarning("Error. Insert a row to database failed.");
                        return;
                    }

                    if (null != mPerson.fullpath) {
                        boolean hasPermission = (ContextCompat.checkSelfPermission(PersonActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

                        if (!hasPermission) {
                            ActivityCompat.requestPermissions(PersonActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    Units.RC_WRITE_STORAGE);
                        } else {
                            String extension = Tools.writePhoto(PersonActivity.this, mPerson.id, new File(mPerson.fullpath));
                            if (null != extension)
                                mPerson.extension = extension;
                            else {
                                showWarning("Error. Copy file failed.");
                                return;
                            }
                        }
                    }

                    getRoom().update(mPerson);
                    finish();
                    break;
                case R.id.menu_refresh:
                    boolean hasPermission = (ContextCompat.checkSelfPermission(PersonActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

                    if (!hasPermission) {
                        ActivityCompat.requestPermissions(PersonActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                Units.RC_BROWSE_IMAGES);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, Units.RC_BROWSE_PHOTO);
                    }
                    break;
                case R.id.menu_remove:
                    getRoom().remove(mPerson);
                    if (null != mPerson.getFilename()) {
                        File photo = new File(mPerson.getFilename());
                        if (photo.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            photo.delete();
                        }
                    }
                    finish();
                    break;
            }
        }
    };

    private final FloatingActionMenu.OnMenuToggleListener onMenuToggleListener = new FloatingActionMenu.OnMenuToggleListener() {
        @Override
        public void onMenuToggle(boolean opened) {
            FloatingActionButton fab = (FloatingActionButton) fMenu.findViewById(R.id.menu_remove);
            if (null == fab)
                return;

            fab.setEnabled(mPerson.id != Units.VAR_NEW_PERSON);
        }
    };

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Toolbar pToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(pToolbar);
        pToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        pToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        CheckBox cbDefault = (CheckBox) findViewById(R.id.cbDefault);
        CheckBox cbAnal = (CheckBox) findViewById(R.id.cbAnal);
        CheckBox cbOral = (CheckBox) findViewById(R.id.cbOral);
        etName = (EditText) findViewById(R.id.etName);
        ImageView btnAddPhoto = (ImageView) findViewById(R.id.imageView);
        fMenu = (FloatingActionMenu) findViewById(R.id.fabMenu);

        findViewById(R.id.menu_done).setOnClickListener(onClickListener);
        findViewById(R.id.menu_refresh).setOnClickListener(onClickListener);
        findViewById(R.id.menu_remove).setOnClickListener(onClickListener);

        wToast = new WarningToast(this);
        fMenu.setClosedOnTouchOutside(true);
        fMenu.setOnMenuToggleListener(onMenuToggleListener);

        btnAddPhoto.setOnClickListener(onClickListener);
        cbDefault.setOnClickListener(onClickListener);
        cbAnal.setOnClickListener(onClickListener);
        cbOral.setOnClickListener(onClickListener);

        Bundle bundle = getIntent().getExtras();

        if (Tools.containsString(bundle, Units.ARG_JSON)) {
            String json = bundle.getString(Units.ARG_JSON);
            this.mPerson = new Gson().fromJson(json, Person.class);

            getSupportActionBar().setTitle(this.mPerson.name);
        } else if (Tools.containsLong(bundle, Units.ARG_INDEX)) {
            long id = bundle.getLong(Units.ARG_INDEX);
            mPerson = getRoom().select(id);

            if (null == mPerson) {
                showWarning(getString(R.string.person_not_found));
                finish();
                return;
            }
            etName.append(mPerson.name);

            if (null != mPerson.getFilename()) {
                File file = getFileStreamPath(mPerson.getFilename());

                Picasso.with(this)
                        .load(file)
                        .into(ivPhoto);
            }
            cbDefault.setChecked(mPerson.traditional);
            cbOral.setChecked(mPerson.oral);
            cbAnal.setChecked(mPerson.anal);

            getSupportActionBar().setTitle(R.string.edit_lovely_note);
        } else {
            mPerson = new Person();
            mPerson.id = Units.VAR_NEW_PERSON;
            getSupportActionBar().setTitle(R.string.new_lovely_note);
        }
        createCustomAnimation();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Units.RC_BROWSE_PHOTO == requestCode && resultCode == Activity.RESULT_OK) {

            if (data == null || data.getData() == null) {
                showWarning("Error. Empty data.");
                return;
            }
            Uri photo = Tools.getSelectedImage(this, data.getData());
            if (photo == null) {
                showWarning("Error. Empty uri.");
                return;
            }

            File file = new File(photo.getPath());
            if (!file.exists()) {
                showWarning("Error. File not exits.");
                return;
            }

            mPerson.fullpath = file.getPath();

            Picasso.with(this)
                    .load(file)
                    .into(ivPhoto);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Units.RC_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String extension = Tools.writePhoto(this, mPerson.id, new File(mPerson.fullpath));
                    if (null != extension)
                        mPerson.extension = extension;
                    else {
                        showWarning("Error. Copy file failed.");
                        return;
                    }
                    getRoom().update(mPerson);
                    //reload my activity with permission granted or use the features what required the permission
                } else {
                    mPerson.fullpath = null;
                    showWarning("The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission.");
                }
                break;
            case Units.RC_BROWSE_IMAGES:
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, Units.RC_BROWSE_PHOTO);
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mPerson) {
            outState.putString(Units.ARG_JSON, mPerson.toJson());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (Tools.containsString(savedInstanceState, Units.ARG_JSON)) {
            String json = savedInstanceState.getString(Units.ARG_JSON);
            this.mPerson = new Gson().fromJson(json, Person.class);
            if (null != mPerson.fullpath)
                Picasso.with(this)
                        .load(new File(mPerson.fullpath))
                        .into(ivPhoto);
        }
    }

    @Override
    public void showWarning(String message) {
        wToast.setText(message);
        wToast.show();
    }

    @Override
    public void hide() {
        wToast.cancel();
    }

    private void createCustomAnimation() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(fMenu.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(fMenu.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(fMenu.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(fMenu.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                fMenu.getMenuIconView().setImageResource(fMenu.isOpened()
                        ? R.drawable.ic_action_navigation_close : R.drawable.ic_action_navigation_menu);
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        fMenu.setIconToggleAnimatorSet(set);
    }

    private IRoom getRoom() {
        return DBHelper.getInstance(this);
    }
}