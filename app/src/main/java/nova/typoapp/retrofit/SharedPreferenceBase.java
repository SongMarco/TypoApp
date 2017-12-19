package nova.typoapp.retrofit;

/**
 * Created by Administrator on 2017-12-04.
 */

/* SharedPreferenceBase

쉐어드를 여러 개 사용할 때 정리해둔 클래스이며,
쉐어드를 다른 클래스(액티비티가 아닌)에서 호출하기 위한 클래스다.

현재는 기능이 필요 없어 주석처리해둔다.

 */


public class SharedPreferenceBase {


//
//    private static Context context;
//
//    public static void setSharedPrefName(String sharedPrefName) {
//        SHARED_PREF_NAME = sharedPrefName;
//    }
//
//    /*
//    getString(R.string.pref_login)으로 가져와야 하는데,
//    이러려면 context가 필요하다. context를 세팅하면 오류가 발생한다.
//    context없이 PREF_NAME을 동적으로 세팅하고 싶은데 잘 안되는군;;
//     */
//    private static String SHARED_PREF_NAME = "pref_login";
//
//    public static SharedPreferenceBase init(Context context) {
//        setContext(context);
//        return new SharedPreferenceBase();
//    }
//
//    public static void setContext(Context context) {
//        SharedPreferenceBase.context = context;
//    }
//
//    protected static void setString(final String key, final String value) {
//        context.getSharedPreferences(SHARED_PREF_NAME, 0).edit().putString(key, value).apply();
//    }
//
//
//
//
//    protected static Set<String> getStringSet(SharedPreferences sharedPreferences, HashSet<String> hashSet) {
//
//        return sharedPreferences.getStringSet("Cookie" ,hashSet);
//    }
//
//    static void putStringSet(SharedPreferences sharedPreferences  ,HashSet<String> cookies ){
//
//        context.getSharedPreferences(SHARED_PREF_NAME, 0).edit().putStringSet("Cookie", cookies).apply();
//
//    }


}