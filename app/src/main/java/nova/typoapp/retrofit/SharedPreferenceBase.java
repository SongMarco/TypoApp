package nova.typoapp.retrofit;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017-12-04.
 */

//쉐어드를 다른 클래스(액티비티가 아닌)에서 호출하기 위한 클래스다.

public class SharedPreferenceBase {

    private static final String SHARED_PREF_NAME = "pref_login";

    private static Context context;

    public static SharedPreferenceBase init(Context context) {
        setContext(context);
        return new SharedPreferenceBase();
    }

    public static void setContext(Context context) {
        SharedPreferenceBase.context = context;
    }

    protected static void setString(final String key, final String value) {
        context.getSharedPreferences(SHARED_PREF_NAME, 0).edit().putString(key, value).apply();
    }



    protected static Set<String> getStringSet(SharedPreferences sharedPreferences, HashSet<String> hashSet) {

        return sharedPreferences.getStringSet("Cookie" ,hashSet);
    }

    static void putStringSet(SharedPreferences sharedPreferences  ,HashSet<String> cookies ){

        context.getSharedPreferences(SHARED_PREF_NAME, 0).edit().putStringSet("Cookie", cookies).apply();

    }


}