package com.ggface.dovvv;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Locale;

public class App extends Application {

    public static void logD(String tag, String message) {
        Log.d(tag, message.toUpperCase(Locale.getDefault()));
    }

    public static File getPIO(Context context, String packageName) {
        File sd = Environment.getExternalStorageDirectory();
        String dataPath;

        try {
            dataPath = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException nnf) {
            dataPath = context.getPackageName();
        }

        if (null != packageName) {
            dataPath = dataPath.replaceFirst(context.getPackageName(), packageName);
        }
        File dataDirectory = new File(sd, dataPath);

        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }
        return dataDirectory;
    }
}