package com.ggface.achivetricks.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

import com.ggface.achivetricks.R;
import com.ggface.achivetricks.UI;
import com.ggface.achivetricks.Units;
import com.ggface.achivetricks.activities.PersonActivity;
import com.ggface.achivetricks.adapters.EditorImagesAdapter;
import com.ggface.achivetricks.classes.DBHelper;
import com.ggface.achivetricks.classes.EditorBodyImage;
import com.ggface.achivetricks.classes.Person;
import com.ggface.achivetricks.classes.RequestCodes;

import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class GalleryFragment extends Fragment {

    private final View.OnClickListener doneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), PersonActivity.class);
//            intent.putExtra(Units.BARCODE, mCode);
//            intent.putExtra(Units.IMAGE_URL, imageUrl);
            startActivityForResult(intent, RequestCodes.RC_PERSON);
        }
    };

    private GridView gvCollection;
    private EditorImagesAdapter adapter;
    private int mPhotoSize, mPhotoSpacing;
    private List<EditorBodyImage> bodyViews;
    private DBHelper dbHelper;

    public GalleryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = new EditorImagesAdapter(getActivity());

        bodyViews = new ArrayList<>();
        dbHelper = DBHelper.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPhotoSize = getResources().getDimensionPixelSize(R.dimen.photo_size);
        mPhotoSpacing = getResources().getDimensionPixelSize(R.dimen.photo_spacing);

        gvCollection = UI.get(view, R.id.gvCollection);
        gvCollection.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(doneClickListener);

        gvCollection.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (adapter.getNumColumns() == 0) {
                    final int numColumns = (int) Math.floor(gvCollection.getWidth() / (mPhotoSize + mPhotoSpacing));
                    if (numColumns > 0) {
                        final int columnWidth = (gvCollection.getWidth() / numColumns) - mPhotoSpacing;
                        adapter.setNumColumns(numColumns);
                        adapter.setItemHeight(columnWidth);

                    }
                }
            }
        });

        gvCollection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), PersonActivity.class);
                intent.putExtra(Units.ARG_INDEX, adapter.getItem(position).id);
                startActivityForResult(intent, RequestCodes.RC_PERSON);
//                if (position == 0) {
//                    Intent intent = new Intent(Intent.ACTION_PICK,
//                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                    startActivityForResult(intent, RequestCodes.RC_BROWSE_PHOTO);
//                } else {
//                    PopupMenu popupMenu = new PopupMenu(getActivity(), view);
//                    popupMenu.getMenuInflater().inflate(R.menu.popup_menu_editor_gallary
//                            , popupMenu.getMenu());
//
//                    popupMenu.getMenu().findItem(R.id.action_upload)
//                            .setVisible(adapter.getItem(position).isLocal());
//
//                    popupMenu.setOnMenuItemClickListener(new OnPopupItemClickListener(position));
//
//                    popupMenu.show();
//
//                }
            }
        });
//        List<Person> items = new ArrayList<>();
//        items.add(new Person("Sophia"));
//        items.add(new Person("Emma"));
//        items.add(new Person("Olivia"));
//        items.add(new Person("Ava"));
//        items.add(new Person("Isabella"));
//        items.add(new Person("Mia"));
//        items.add(new Person("Zoe"));
//        items.add(new Person("Lily"));
//        items.add(new Person("Emily"));
//        items.add(new Person("Madelyn"));
//        items.add(new Person("Madison"));
//        items.add(new Person("Chloe"));
//        items.add(new Person("Charlotte"));
//        items.add(new Person("Aubrey"));
        List<Person> items = readDB();
        UI.text(getActivity(), "size: " + items.size());
        adapter.rewrite(items);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Person> items = readDB();
        UI.text(getActivity(), "size: " + items.size());
        adapter.rewrite(items);
    }

    private List<Person> readDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        Cursor c;
        try {
//            c = db.query("GIRLS", new String[] {"PH_NAME", "PH_CUNT", "PH_ASS", "PH_MINET", "PH_PHOTO"}, null, null, null, null, null);
//            c = db.query("GIRLS", null, null, null, null, null, null);
            c = db.rawQuery("SELECT * FROM girls", null);
        } catch (Exception e) {
            return null;
        }
        UI.text(getActivity(), "count: " + c.getCount());
        // ставим позицию курсора на первую строку выборки
        // если в выборке нет строк, вернется false
        List<Person> list = new ArrayList<>();
        if (c.moveToFirst()) {

            // определяем номера столбцов по имени в выборке
            int idColIndex = c.getColumnIndex("id");
            int nameColIndex = c.getColumnIndex("girl_name");
            int pussyColIndex = c.getColumnIndex("pussy");
            int analColIndex = c.getColumnIndex("anal");
            int oralColIndex = c.getColumnIndex("oral");
            int photoColIndex = c.getColumnIndex("girl_photo");
            Person item = new Person();
            do {
                item.id = c.getInt(idColIndex);
                item.name = c.getString(nameColIndex);

                item.traditional = c.getInt(pussyColIndex) == 1;
                item.anal = c.getInt(analColIndex) == 1;
                item.oral = c.getInt(oralColIndex) == 1;

                byte[] byteArray = c.getBlob(photoColIndex);
                if (byteArray != null)
                    item.image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                // получаем значения по номерам столбцов и пишем все в лог
//                Log.d(LOG_TAG,
//                        "ID = " + c.getInt(idColIndex) +
//                                ", name = " + c.getString(nameColIndex) +
//                                ", email = " + c.getString(emailColIndex));
                // переход на следующую строку
                // а если следующей нет (текущая - последняя), то false - выходим из цикла
                list.add(item);
            } while (c.moveToNext());
        } else
//            Log.d(LOG_TAG, "0 rows");
            c.close();
        return list;
    }
}
