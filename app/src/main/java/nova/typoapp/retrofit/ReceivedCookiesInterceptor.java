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

/* ReceivedCookiesInterceptor

레트로핏 http 통신을 수행할 때 사용한다.
서버로부터 받은 세션 ID(PHPSESSID)를 쉐어드에 저장한다.

 */
public class ReceivedCookiesInterceptor implements Interceptor {

    //SharedPreferences 를 불러오기 위해 컨텍스트 세팅
    Context context;

    public ReceivedCookiesInterceptor(Context context) {
        this.context = context;
    }

    /*
        response 로부터 쿠키를 받아와 쉐어드에 저장한다.
        현재는 PHPSESSID 쿠키를 저장한다.
    */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());

        //먼저 SharedPreferences 를 가져온다.
        SharedPreferences pref_login = context.getSharedPreferences(context.getString(R.string.key_pref_Login) , 0);

        //response 에 쿠키값이 세팅되있다면 가져와서 SharedPreferences 에 저장한다.
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>();

       /*      cookies HashSet 에 "Set-Cookie"이름으로 된 정보 저장
             ex) 저장 형식 : ["PHPSESSID=9mhcq88airavllfflmib6i2qk5; path=\/"]
             PHPSESSID는 세션 ID값이며, PATH는 세션의 저장 경로다.
                          */
            cookies.addAll(originalResponse.headers("Set-Cookie"));

            // SharedPreferences 에  cookies 를 넣어주는 작업을 수행
            pref_login.edit().putStringSet("Cookie", cookies).apply();

        }

        return originalResponse;
    }
}