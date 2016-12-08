package com.ggface.dovvv.fragments;

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
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ggface.dovvv.App;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PersonFragment extends Fragment implements WarningToast.OnToastListener {

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imageView:
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, Units.RC_BROWSE_PHOTO);
                    break;
                case R.id.cbDefault:
                    mPerson.traditional = ((CheckBox) v).isChecked();
                    break;
                case R.id.cbAnal:
                    mPerson.anal = ((CheckBox) v).isChecked();
                    break;
                case R.id.cbOral:
                    mPerson.oral = ((CheckBox) v).isChecked();
                    break;
            }
        }
    };

    private Toast wToast;
    private Person mPerson;

    @BindView(R.id.ivPhoto)
    ImageView ivPhoto;

    @BindView(R.id.cbDefault)
    CheckBox cbDefault;

    @BindView(R.id.cbAnal)
    CheckBox cbAnal;

    @BindView(R.id.cbOral)
    CheckBox cbOral;

    @BindView(R.id.etName)
    EditText etName;

    @BindView(R.id.imageView)
    ImageView btnAddPhoto;

    @BindView(R.id.fabMenu)
    FloatingActionMenu fMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_person, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        wToast = new WarningToast(getActivity());
        fMenu.setClosedOnTouchOutside(true);
        fMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                FloatingActionButton fab = (FloatingActionButton) fMenu.findViewById(R.id.menu_remove);
                if (null == fab)
                    return;

                fab.setEnabled(mPerson.id != Units.VAR_NEW_PERSON);

                fab = (FloatingActionButton) fMenu.findViewById(R.id.menu_refresh);
                if (null == fab)
                    return;

                fab.setEnabled(null != mPerson.getFilename());
            }
        });

        btnAddPhoto.setOnClickListener(onClickListener);
        cbDefault.setOnClickListener(onClickListener);
        cbAnal.setOnClickListener(onClickListener);
        cbOral.setOnClickListener(onClickListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Units.RC_BROWSE_PHOTO == requestCode && resultCode == Activity.RESULT_OK) {

            if (data == null || data.getData() == null) {
                showWarning("Error. Empty data.");
                return;
            }
            Uri photo = Tools.getSelectedImage(getActivity(), data.getData());
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

            Picasso.with(getActivity())
                    .load(file)
                    .into(ivPhoto);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getActivity().getIntent().getExtras();

        if (Tools.containsString(bundle, Units.ARG_JSON)) {
            String json = bundle.getString(Units.ARG_JSON);
            this.mPerson = new Gson().fromJson(json, Person.class);

            Tools.getBar(this).setTitle(this.mPerson.name);
        } else if (Tools.containsLong(bundle, Units.ARG_INDEX)) {
            long id = bundle.getLong(Units.ARG_INDEX);
            mPerson = getRoom().select(id);

            if (null == mPerson) {
                showWarning(getString(R.string.person_not_found));
                getActivity().finish();
                return;
            }
            etName.append(mPerson.name);

            if (null != mPerson.getFilename()) {
                File file = App.getContext().getFileStreamPath(mPerson.getFilename());

                Picasso.with(getActivity())
                        .load(file)
                        .into(ivPhoto);
            }
            cbDefault.setChecked(mPerson.traditional);
            cbOral.setChecked(mPerson.oral);
            cbAnal.setChecked(mPerson.anal);

            Tools.getBar(this).setTitle(R.string.edit_lovely_note);
        } else {
            mPerson = new Person();
            mPerson.id = Units.VAR_NEW_PERSON;
            Tools.getBar(this).setTitle(R.string.new_lovely_note);
        }
        createCustomAnimation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Units.RC_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String extension = Tools.writePhoto(mPerson.id, new File(mPerson.fullpath));
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
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mPerson) {
            outState.putString(Units.ARG_JSON, mPerson.toJson());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (Tools.containsString(savedInstanceState, Units.ARG_JSON)) {
            String json = savedInstanceState.getString(Units.ARG_JSON);
            this.mPerson = new Gson().fromJson(json, Person.class);
            if (null != mPerson.fullpath)
                Picasso.with(getActivity())
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

    @OnClick(R.id.menu_done)
    void onClickDone() {
        mPerson.name = etName.getText().toString();

        if (Units.VAR_NEW_PERSON == mPerson.id) {
            mPerson.id = getRoom().insert(mPerson);
        }

        if (Units.VAR_NEW_PERSON == mPerson.id) {
            showWarning("Error. Insert a row to database failed.");
            return;
        }

        if (null != mPerson.fullpath) {
            boolean hasPermission = (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

            if (!hasPermission) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        Units.RC_WRITE_STORAGE);
            } else {
                String extension = Tools.writePhoto(mPerson.id, new File(mPerson.fullpath));
                if (null != extension)
                    mPerson.extension = extension;
                else {
                    showWarning("Error. Copy file failed.");
                    return;
                }
            }
        }

        getRoom().update(mPerson);
        getActivity().finish();
    }

    @OnClick(R.id.menu_refresh)
    void onClickRefresh() {
        mPerson.extension = null;
        mPerson.fullpath = null;
        ivPhoto.setImageBitmap(null);
    }

    @OnClick(R.id.menu_remove)
    void onClickRemove() {
        getRoom().remove(mPerson);
        if (null != mPerson.getFilename()) {
            File photo = new File(mPerson.getFilename());
            if (photo.exists())
                photo.delete();
        }
        getActivity().finish();
    }

    private IRoom getRoom() {
        return DBHelper.getInstance(getActivity());
    }
}
