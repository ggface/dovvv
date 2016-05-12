package com.ggface.dovvv.classes;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.ggface.dovvv.App;
import com.ggface.dovvv.Units;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Tools {

    public static boolean containsString(Bundle src, String key) {
        return src != null && !TextUtils.isEmpty(src.getString(key));
    }

    public static boolean containsInt(Bundle src, String key) {
        return src != null && (Integer.valueOf(-1).compareTo(src.getInt(key, -1)) != 0);
    }

    public static boolean containsLong(Bundle src, String key) {
        return src != null && (Long.valueOf(-1).compareTo(src.getLong(key, -1)) != 0);
    }

    public static <T> T fromMessage(Message msg, Class<T> classOfT) {
        Object object = msg.obj;
        return classOfT.cast(object);
    }

    public static String trimDoubleNewline(String source) {
        if (TextUtils.isEmpty(source))
            return Units.EMPTY;

        String lines[] = source.split("\\r?\\n");
        List<String> trimList = new ArrayList<String>();

        for (int i = 0; i < lines.length; i++) {
            String trimLine = lines[i].trim();

            if (trimList.isEmpty()) {
                trimList.add(trimLine);
            } else {
                if (TextUtils.isEmpty(trimLine)) {
                    String previous = trimList.get(trimList.size() - 1);
                    if (!trimLine.equals(previous)) {
                        trimList.add(trimLine);
                    }
                } else {
                    trimList.add(trimLine);
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < trimList.size(); i++)
            if (!Units.EMPTY.equals(trimList.get(i)))
                sb.append(trimList.get(i)).append("\n");
            else
                sb.append("\n");


        return sb.toString().trim();
    }

    public static boolean compareDates(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isGoogleMapsInstalled(Context c) {
        try {
            c.getPackageManager().getApplicationInfo(
                    "com.google.android.apps.maps", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static Uri getSelectedImage(Context c, Uri uri) {
        String picturePath;

        try {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = c.getContentResolver().query(uri,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor
                    .getColumnIndex(MediaStore.Images.Media.DATA);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

            return Uri.parse(picturePath);
        } catch (Exception e) {
            return null;
        }
    }

    static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    public static String getSignedNumber(double value) {
        String src = String.valueOf(value);

        if (src.contains("."))
            src = src.substring(0, src.lastIndexOf("."));

        return src;
    }

    static String getOSVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    private static int getSymbol(byte[] text, int position) {
        if (position >= 0)
            return text[position] < 0 ? text[position] + 256 : text[position];
        else
            return text[text.length + position] < 0 ? text[text.length
                    + position] + 256 : text[text.length + position];
    }

    public static String decode(byte[] key, byte[] content) {
        byte[] decode = new byte[content.length];
        for (int i = 0; i < content.length; i++) {

            int srcCharCode = getSymbol(content, i);
            int keyCharCode = getSymbol(key, (i % key.length) - 1);

            int value = srcCharCode - keyCharCode;
            if (value < 0)
                value = (srcCharCode - keyCharCode) + 256;
            else if (value > 127)
                value = value + 0;

            decode[i] = (byte) value;
        }

        return new String(decode);
    }

//    public static String getLocaleDate(Date value, boolean prettyFormat) {
//        return getLocaleDate(value, prettyFormat, false);
//    }

//    public static String getLocaleDate(Date value, boolean prettyFormat, boolean shortFormat) {
//        if (value == null)
//            return App.getRes().getString(R.string.date_unknown);
//
//        if (!prettyFormat)
//            return getViewDateFormat(value, shortFormat);
//
//        final Calendar today, yesterday, current;
//        today = Calendar.getInstance();
//        yesterday = Calendar.getInstance();
//        yesterday.add(Calendar.DATE, -1);
//        current = Calendar.getInstance();
//        current.setTime(value);
//
//        String time = getCustomDateFormat(value, "HH:mm") + ", ";
//
//        if (Tools.compareDates(today, current))
//            return (shortFormat ? Units.EMPTY : time) + App.getRes().getString(R.string.date_today);
//        else if (Tools.compareDates(yesterday, current))
//            return (shortFormat ? Units.EMPTY : time) + App.getRes().getString(R.string.date_yesterday);
//        else
//            return getViewDateFormat(value, shortFormat);
//    }

//    private static String getViewDateFormat(Date value, boolean shortFormat) {
//        return new SimpleDateFormat(shortFormat ? Units.VIEW_DATE_SHORT_FORMAT
//                : Units.VIEW_DATE_FORMAT, Locale.getDefault()).format(value);
//    }

    public static String getCustomDateFormat(Date value, String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(value);
    }

//    public static int getDrawableId(String res) {
//        return App.getRes().getIdentifier(res, "drawable", App.getAppPackageName());
//    }

    public static FragmentManager from(Fragment fragment) {
        return fragment.getActivity().getSupportFragmentManager();
    }

    public static FragmentManager from(AppCompatActivity activity) {
        return activity.getSupportFragmentManager();
    }

    public static ActionBar getBar(Fragment instance) {
        return ((AppCompatActivity) instance.getActivity()).getSupportActionBar();
    }

    public static String writePhoto(long index, File src) {
        String filenameArray[] = src.getName().split("\\.");
        String extension = filenameArray[filenameArray.length - 1];
        String newFilename = String.valueOf(index) + '.' + extension;

        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = App.getContext().openFileOutput(newFilename, Context.MODE_PRIVATE);
            byte[] buff = new byte[1024];
            int len;
            while ((len = is.read(buff)) > 0) {
                os.write(buff, 0, len);
            }
            is.close();
            os.close();
            return extension;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (is != null)
                    is.close();
                if (os != null)
                    os.close();
            } catch (IOException io) {
            }
        }
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

}
