package com.ggface.achivetricks;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.util.Locale;


@ReportsCrashes(
        formUri = "https://collector.tracepot.com/09fc05db"
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

    public static Resources getRes() {
        return sContext.getResources();
    }

    public static void logD(String tag, String message) {
        Log.d(tag, message.toUpperCase(Locale.getDefault()));
    }
}
