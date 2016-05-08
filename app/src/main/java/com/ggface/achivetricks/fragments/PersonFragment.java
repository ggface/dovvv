package com.ggface.achivetricks.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
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

import com.ggface.achivetricks.R;
import com.ggface.achivetricks.UI;
import com.ggface.achivetricks.Units;
import com.ggface.achivetricks.classes.DBHelper;
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

    private final View.OnClickListener doneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            person.name = etName.getText().toString();
            if (person.id == 0)
                add();
            else
                update();
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
    private CheckBox cbDefault, cbAnal, cbOral;
    private EditText etName;

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

            Picasso.with(getActivity())
                    .load(new File(file.getPath()))
                    .into(ivPhoto);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        long id = getActivity().getIntent().getLongExtra(Units.ARG_INDEX, -1);

        if (0 <= id) {
            person = load(id);
            etName.setText(person.name);
            if (null != person.image)
                ivPhoto.setImageBitmap(person.image);

            cbDefault.setChecked(person.traditional);
            cbOral.setChecked(person.oral);
            cbAnal.setChecked(person.anal);
        }
    }

    private Person load(long id) {
        Person item = null;
        SQLiteDatabase db = DBHelper.getInstance(getActivity()).getWritableDatabase();
        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        Cursor c;
        try {
//            c = db.query("GIRLS", new String[] {"PH_NAME", "PH_CUNT", "PH_ASS", "PH_MINET", "PH_PHOTO"}, null, null, null, null, null);
//            c = db.query("GIRLS", null, null, null, null, null, null);
            c = db.rawQuery("SELECT * FROM girls WHERE id = " + id, null);
        } catch (Exception e) {
            return null;
        }
        UI.text(getActivity(), "count: " + c.getCount());
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
//            List<Person> list = new ArrayList<>();
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("girl_name");
            int pussyColIndex = c.getColumnIndex("pussy");
            int analColIndex = c.getColumnIndex("anal");
            int oralColIndex = c.getColumnIndex("oral");
            int photoColIndex = c.getColumnIndex("girl_photo");
            item = new Person();
            item.id = c.getInt(idColIndex);
            item.name = c.getString(nameColIndex);

            item.traditional = c.getInt(pussyColIndex) == 1;
            item.anal = c.getInt(analColIndex) == 1;
            item.oral = c.getInt(oralColIndex) == 1;

            byte[] byteArray = c.getBlob(photoColIndex);
            if (byteArray != null)
                item.image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }


        c.close();
        return item;

    }

    private void add() {
        ContentValues values = new ContentValues();
        // Задайте значения для каждого столбца
        values.put("girl_name", person.name);
        values.put("pussy", person.traditional ? 1 : 0);
        values.put("oral", person.oral ? 1 : 0);
        values.put("anal", person.anal ? 1 : 0);
        // Вставляем данные в таблицу
        UI.text(getActivity(), "" + DBHelper.getInstance(getActivity()).add(values));
    }

    private void update() {
        ContentValues values = new ContentValues();
        // Задайте значения для каждого столбца
        values.put("girl_name", person.name);
        values.put("pussy", person.traditional ? 1 : 0);
        values.put("oral", person.oral ? 1 : 0);
        values.put("anal", person.anal ? 1 : 0);
        // Вставляем данные в таблицу
        UI.text(getActivity(), "" + DBHelper.getInstance(getActivity()).upd(person.id, values));
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
