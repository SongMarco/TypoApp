package nova.typoapp.retrofit;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.HashSet;

import nova.typoapp.R;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by Administrator on 2017-12-04.
 */

public class ReceivedCookiesInterceptor implements Interceptor {

    Context context;

    public ReceivedCookiesInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        SharedPreferences pref_login = context.getSharedPreferences(context.getString(R.string.key_pref_Login) , 0);
        SharedPreferenceBase.setContext(context);

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>();

            cookies.addAll(originalResponse.headers("Set-Cookie"));

            // Preference에 cookies를 넣어주는 작업을 수행
            SharedPreferenceBase.putStringSet(pref_login, cookies);

        }

        return originalResponse;
    }
}