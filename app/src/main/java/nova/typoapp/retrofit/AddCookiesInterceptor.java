package nova.typoapp.retrofit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import nova.typoapp.R;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AddCookiesInterceptor implements Interceptor {

    Context context;

    public AddCookiesInterceptor(Context context) {
        this.context = context;
    }



    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();


        SharedPreferenceBase.setContext(context);

        SharedPreferences pref_login = context.getSharedPreferences(context.getString(R.string.key_pref_Login) , 0);

        Set<String> preferences = pref_login.getStringSet("Cookie" , new HashSet<String>() );

        Log.e("my", "intercept: "+preferences.toString() );
//        Log.e("my", "shared-in intercept: "+context.getSharedPreferences("pref_login" , 0 ).getAll()  ) ;

        for (String cookie : preferences) {
            builder.addHeader("Cookie", cookie);
        }

        // Web,Android,iOS 구분을 위해 User-Agent세팅
        builder.removeHeader("User-Agent").addHeader("User-Agent", "Android");


        return chain.proceed(builder.build());
    }
}