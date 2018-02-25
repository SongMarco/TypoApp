package nova.typoapp;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by Administrator on 2017-12-04.
 */


public class MyApplication extends Application {

    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }

}
