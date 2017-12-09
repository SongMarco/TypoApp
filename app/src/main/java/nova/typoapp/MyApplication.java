package nova.typoapp;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

/**
 * Created by Administrator on 2017-12-04.
 */


public class MyApplication extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
