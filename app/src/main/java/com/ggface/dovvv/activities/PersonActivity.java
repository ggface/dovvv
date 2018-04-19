package com.ggface.dovvv.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
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
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;

public class PersonActivity extends AppCompatActivity {

    private static final String EXTRA_PERSON_ID = "EXTRA_PERSON_ID";

    private Toast mToast;
    private Person mPerson;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.name_edit_text)
    EditText mNameEditText;

    @BindView(R.id.traditional_check_box)
    CheckBox mTraditionalCheckBox;

    @BindView(R.id.oral_check_box)
    CheckBox mOralCheckBox;

    @BindView(R.id.anal_check_box)
    CheckBox mAnalCheckBox;

    @BindView(R.id.photo_image_view)
    ImageView mPhotoImageView;

    @BindView(R.id.fab_menu_button)
    FloatingActionMenu mFabMenuButton;

    @BindView(R.id.menu_done)
    View mMenuDone;

    @BindView(R.id.menu_photo)
    View mMenuPhoto;

    @BindView(R.id.menu_remove)
    View mMenuRemove;

    private final View.OnClickListener mOnCheckBoxClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.traditional_check_box:
                    mPerson.traditional = mTraditionalCheckBox.isChecked();
                    break;
                case R.id.oral_check_box:
                    mPerson.oral = mOralCheckBox.isChecked();
                    break;
                case R.id.anal_check_box:
                    mPerson.anal = mAnalCheckBox.isChecked();
                    break;
            }
        }
    };

    public static void startActivity(@NonNull Activity activity, long personId) {
        Intent intent = new Intent(activity, PersonActivity.class);
        intent.putExtra(EXTRA_PERSON_ID, personId);
        activity.startActivityForResult(intent, Units.RC_PERSON);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        mMenuDone.setOnClickListener(v -> {
            mPerson.name = mNameEditText.getText().toString();

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
                    if (null != extension) {
                        mPerson.extension = extension;
                    } else {
                        showWarning("Error. Copy file failed.");
                        return;
                    }
                }
            }

            getRoom().update(mPerson);
            finish();
        });
        mMenuPhoto.setOnClickListener(v -> {
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
        });
        mMenuRemove.setOnClickListener(v -> {
            getRoom().remove(mPerson);
            if (null != mPerson.getFilename()) {
                File photo = new File(mPerson.getFilename());
                if (photo.exists()) {
                    photo.delete();
                }
            }
            finish();
        });
        mToast = new WarningToast(this);
        mFabMenuButton.setClosedOnTouchOutside(true);
        mFabMenuButton.setOnMenuToggleListener(opened -> mMenuRemove.setEnabled(mPerson.id != Units.VAR_NEW_PERSON));

        mTraditionalCheckBox.setOnClickListener(mOnCheckBoxClickListener);
        mOralCheckBox.setOnClickListener(mOnCheckBoxClickListener);
        mAnalCheckBox.setOnClickListener(mOnCheckBoxClickListener);

        Bundle bundle = getIntent().getExtras();

        if (Tools.containsString(bundle, Units.ARG_JSON)) {
            String json = bundle.getString(Units.ARG_JSON);
            this.mPerson = new Gson().fromJson(json, Person.class);

            getSupportActionBar().setTitle(this.mPerson.name);
        } else if (Tools.containsLong(bundle, EXTRA_PERSON_ID)) {
            long id = bundle.getLong(EXTRA_PERSON_ID);
            mPerson = getRoom().select(id);

            if (null == mPerson) {
                showWarning(getString(R.string.person_not_found));
                finish();
                return;
            }
            mNameEditText.append(mPerson.name);

            if (null != mPerson.getFilename()) {
                File file = getFileStreamPath(mPerson.getFilename());

                Picasso.with(this)
                        .load(file)
                        .into(mPhotoImageView);
            }
            mTraditionalCheckBox.setChecked(mPerson.traditional);
            mOralCheckBox.setChecked(mPerson.oral);
            mAnalCheckBox.setChecked(mPerson.anal);

            getSupportActionBar().setTitle(R.string.edit_lovely_note);
        } else {
            mPerson = new Person();
            mPerson.id = Units.VAR_NEW_PERSON;
            getSupportActionBar().setTitle(R.string.new_lovely_note);
        }
        createCustomAnimation();
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
                    .into(mPhotoImageView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Units.RC_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String extension = Tools.writePhoto(this, mPerson.id, new File(mPerson.fullpath));
                    if (null != extension) {
                        mPerson.extension = extension;
                    } else {
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
            outState.putString(Units.ARG_JSON, new Gson().toJson(mPerson, Person.class));
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (Tools.containsString(savedInstanceState, Units.ARG_JSON)) {
            String json = savedInstanceState.getString(Units.ARG_JSON);
            this.mPerson = new Gson().fromJson(json, Person.class);
            if (null != mPerson.fullpath) {
                Picasso.with(this)
                        .load(new File(mPerson.fullpath))
                        .into(mPhotoImageView);
            }
        }
    }

    private void showWarning(String message) {
        mToast.cancel();
        mToast.setText(message);
        mToast.show();
    }

    private void createCustomAnimation() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(mFabMenuButton.getMenuIconView(), "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(mFabMenuButton.getMenuIconView(), "scaleY", 1.0f, 0.2f);

        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(mFabMenuButton.getMenuIconView(), "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(mFabMenuButton.getMenuIconView(), "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);

        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFabMenuButton.getMenuIconView().setImageResource(mFabMenuButton.isOpened()
                        ? R.drawable.ic_action_navigation_close : R.drawable.ic_action_navigation_menu);
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));

        mFabMenuButton.setIconToggleAnimatorSet(set);
    }

    private IRoom getRoom() {
        return DBHelper.getInstance(this);
    }
}