package com.ggface.dovvv.classes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ggface.dovvv.App;
import com.ggface.dovvv.UI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class IOUtils {

    private static final String FILE_AFFIX = "dovvv_photo_";

    public static void clearOutdatedFiles(@NonNull Context context, @Nullable List<Person> persons) {
        List<File> existsPhotos = new ArrayList<>();

        if (null != persons) {
            for (Person person : persons) {
                if (null != person.getFilename()) {
                    File src = context.getFileStreamPath(person.getFilename());
                    existsPhotos.add(src);
                }
            }
        }

        List<File> allFiles = Arrays.asList(context.getFilesDir().listFiles());

        if (allFiles.size() > 0) {
            Iterator<File> it = allFiles.iterator();
            //noinspection WhileLoopReplaceableByForEach
            while (it.hasNext()) {
                File file = it.next();
                String filename = file.getName();
                if (filename.contains(FILE_AFFIX)) {
                    if (!existsPhotos.contains(file)) {
                        file.delete();
                    }
                }
            }
        }
    }

    public static void exportData(@NonNull Context context, @Nullable List<Person> persons) {
        final List<Person> personsList = CollectionUtils.wrapListNonNull(persons);
        try {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .create();

            String json = gson.toJson(personsList,
                    new TypeToken<List<Person>>() {
                    }.getType());

            File folder = new File(App.getPIO(context, null),
                    Tools.getCustomDateFormat(new Date(), "yyyy-MM-dd HHmmss") + " backup");
            if (!folder.exists()) {
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
            for (int i = 0; i < personsList.size(); i++) {
                Person person = personsList.get(i);
                if (null != person.getFilename()) {
                    File src = context.getFileStreamPath(person.getFilename());
                    Tools.copyFile(src, new File(folder, person.getFilename()));
                }
            }
        } catch (Exception e) {
            UI.text(context, e.getMessage());
        }
    }

    @NonNull
    public static List<Person> importData(@NonNull Context context, @NonNull String srcPath) {
        final List<Person> result = new ArrayList<>();
        try {
            DBHelper.getInstance(context).clear();

            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation().create();

            File dir = new File(srcPath);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dir + File.separator + "backup.json")));

            result.addAll(gson.fromJson(br,
                    new TypeToken<List<Person>>() {
                    }.getType()));

            for (int i = 0; i < result.size(); i++) {
                Person p = result.get(i);
                if (p.extension != null) {
                    File photo = new File(dir + File.separator + "dovvv_photo_" + p.id + '.' + p.extension);
                    if (photo.exists()) {
                        Tools.writePhoto(context, p.id, photo.getAbsoluteFile());
                    }
                }
                DBHelper.getInstance(context).insert(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @NonNull
    public static List<Person> getBackupList(@NonNull Context context) {
        final List<Person> result = new ArrayList<>();
        File[] files = App.getPIO(context, null).listFiles();

        if (null != files) {
            int iterator = -1;
            for (int i = 0; i < files.length; i++) {
                File inFile = files[i];
                if (inFile.isDirectory()) {
                    Person p = new Person();
                    p.id = iterator--;
                    p.fullpath = inFile.getAbsolutePath();
                    p.name = inFile.getName();
                    p.extension = String.valueOf((int) Tools.folderSize(inFile) / 1024 / 1024) + " mB";
                    result.add(p);
                }
            }
        }
        return result;
    }
}