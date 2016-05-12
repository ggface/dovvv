package com.ggface.achivetricks.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.ggface.achivetricks.App;
import com.ggface.achivetricks.R;
import com.ggface.achivetricks.UI;
import com.ggface.achivetricks.Units;
import com.ggface.achivetricks.classes.DBHelper;
import com.ggface.achivetricks.classes.Person;
import com.ggface.achivetricks.classes.RequestCodes;
import com.ggface.achivetricks.classes.Tools;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.File;

public class PersonFragment extends Fragment {

    private final View.OnClickListener doneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mPerson.name = etName.getText().toString();

            if (Units.VAR_NEW_PERSON == mPerson.id) {
                mPerson.id = DBHelper.getInstance(getActivity()).insert(mPerson);
            }

            if (Units.VAR_NEW_PERSON == mPerson.id) {
                UI.text(getActivity(), "Error #1");
                return;
            }

            if (null != mPerson.fullpath) {
                String extension = Tools.writePhoto(mPerson.id, new File(mPerson.fullpath));
                if (null != extension)
                    mPerson.extension = extension;
                else {
                    UI.text(getActivity(), "Error #2");
                    return;
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

    private Person mPerson;
    private ImageView ivPhoto;
    private CheckBox cbDefault, cbAnal, cbOral;
    private EditText etName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_person, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnAddPhoto = (ImageView) view.findViewById(R.id.imageView);
        cbDefault = (CheckBox) view.findViewById(R.id.cbDefault);
        cbAnal = (CheckBox) view.findViewById(R.id.cbAnal);
        cbOral = (CheckBox) view.findViewById(R.id.cbOral);
        ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto);
        etName = (EditText) view.findViewById(R.id.etName);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
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
//                WarningDialog.showInstance(Tools.from(this), b);
                return;
            }
            Uri photo = Tools.getSelectedImage(getActivity(), data.getData());
            if (photo == null) {
//                WarningDialog.showInstance(Tools.from(this), b);
                return;
            }

            File file = new File(photo.getPath());
            if (!file.exists()) {
//                WarningDialog.showInstance(Tools.from(this), b);
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
                UI.text(getActivity(), "A person not found");
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

            Tools.getBar(this).setTitle("Edit lovely note");
        } else {
            mPerson = new Person();
            mPerson.id = Units.VAR_NEW_PERSON;
            Tools.getBar(this).setTitle("New lovely note");
        }
    }
}
