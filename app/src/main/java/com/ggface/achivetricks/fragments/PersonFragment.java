package com.ggface.achivetricks.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.ggface.achivetricks.R;
import com.ggface.achivetricks.UI;
import com.ggface.achivetricks.classes.Person;
import com.ggface.achivetricks.classes.RequestCodes;
import com.ggface.achivetricks.classes.Tools;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PersonFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PersonFragment extends Fragment {

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnAddPhoto:
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, RequestCodes.RC_BROWSE_PHOTO);
                    break;
                case R.id.cbDefault:
                    person.traditional = ((CheckBox) v).isChecked();
                    break;
                case R.id.cbAnal:
                    person.anal = ((CheckBox) v).isChecked();
                    break;
                case R.id.cbOral:
                    person.oral = ((CheckBox) v).isChecked();
                    break;
            }
        }
    };

    private OnFragmentInteractionListener mListener;
    private Person person;
    private ImageView ivPhoto;

    public PersonFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        person = new Person();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_person, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnAddPhoto = (Button) view.findViewById(R.id.btnAddPhoto);
        CheckBox cbDefault = (CheckBox) view.findViewById(R.id.cbDefault);
        CheckBox cbAnal = (CheckBox) view.findViewById(R.id.cbAnal);
        CheckBox cbOral = (CheckBox) view.findViewById(R.id.cbOral);
        ivPhoto = (ImageView) view.findViewById(R.id.ivPhoto);

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

            Picasso.with(getActivity())
                    .load(new File(file.getPath()))
                    .into(ivPhoto);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
