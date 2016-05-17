package com.ggface.dovvv.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ggface.dovvv.App;
import com.ggface.dovvv.R;
import com.ggface.dovvv.Units;
import com.ggface.dovvv.classes.DBHelper;
import com.ggface.dovvv.classes.Person;
import com.ggface.dovvv.classes.RequestCodes;
import com.ggface.dovvv.classes.Tools;
import com.ggface.dovvv.widgets.WarningToast;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PersonFragment extends Fragment implements WarningToast.OnToastListener {

    private static final int REQUEST_WRITE_STORAGE = 112;

    private final View.OnClickListener doneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPerson.name = etName.getText().toString();

            if (Units.VAR_NEW_PERSON == mPerson.id) {
                mPerson.id = DBHelper.getInstance(getActivity()).insert(mPerson);
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
                            REQUEST_WRITE_STORAGE);
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

            DBHelper.getInstance(getActivity()).update(mPerson);
            getActivity().finish();
        }
    };

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imageView:
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, RequestCodes.RC_BROWSE_PHOTO);
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

    @Bind(R.id.ivPhoto)
    ImageView ivPhoto;

    @Bind(R.id.cbDefault)
    CheckBox cbDefault;

    @Bind(R.id.cbAnal)
    CheckBox cbAnal;

    @Bind(R.id.cbOral)
    CheckBox cbOral;

    @Bind(R.id.etName)
    EditText etName;

    @Bind(R.id.imageView)
    ImageView btnAddPhoto;

    @Bind(R.id.fab)
    FloatingActionButton fab;

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

        fab.setOnClickListener(doneClickListener);
        btnAddPhoto.setOnClickListener(onClickListener);
        cbDefault.setOnClickListener(onClickListener);
        cbAnal.setOnClickListener(onClickListener);
        cbOral.setOnClickListener(onClickListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RequestCodes.RC_BROWSE_PHOTO == requestCode && resultCode == Activity.RESULT_OK) {

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
            mPerson = DBHelper.getInstance(getActivity()).select(id);

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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String extension = Tools.writePhoto(mPerson.id, new File(mPerson.fullpath));
                    if (null != extension)
                        mPerson.extension = extension;
                    else {
                        showWarning("Error. Copy file failed.");
                        return;
                    }
                    DBHelper.getInstance(getActivity()).update(mPerson);
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
}