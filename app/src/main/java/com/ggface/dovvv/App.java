package com.ggface.dovvv;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.util.Locale;


@ReportsCrashes(
        formUri = "https://collector.tracepot.com/494237a9"
)
public class App extends Application {

    public static final Locale LOCALE = Locale.getDefault();

    private static Context sContext;

    @Override
    public void onCreate() {
        sContext = getBaseContext();
        super.onCreate();
        ACRA.init(this);
    }

    public static Boolean isDev() {
        // DON'T TOUCH
        if (!BuildConfig.DEBUG)
            return false;

        // SET DEV MODE AS INIT VALUE
        return !true;
    }


    public static Resources getRes() {
        return sContext.getResources();
    }

    public static void logD(String tag, String message) {
        Log.d(tag, message.toUpperCase(Locale.getDefault()));
    }

    public static File getPIO(String packageName) {
        File sd = Environment.getExternalStorageDirectory();
        String dataPath;


            try {
                dataPath = sContext.getPackageManager().getPackageInfo(
                        sContext.getPackageName(), 0).applicationInfo.dataDir;
            } catch (PackageManager.NameNotFoundException nnf) {
                dataPath = sContext.getPackageName();
            }

        if (null != packageName)
            dataPath = dataPath.replaceFirst(sContext.getPackageName(), packageName);

        File dataDirectory = new File(sd, dataPath);

        if (!dataDirectory.exists())
            dataDirectory.mkdirs();

        return dataDirectory;
    }

    public static Context getContext() {
        return sContext;
    }
}
