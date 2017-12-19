package nova.typoapp.retrofit;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import nova.typoapp.R;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/* AddCookiesInterceptor

레트로핏 http 통신을 수행할 때 사용한다.
세션 정보가 필요한 경우(로그인 정보 확인 - 프로필, 게시물 수정 삭제 등)에 세팅하는 인터셉터.

여기서는 sharedReferences 로부터 쿠키 정보를 가져와 세팅하게 된다.
 */

public class AddCookiesInterceptor implements Interceptor {

    //컨텍스트 세팅 -> getSharedPreferences 콜하기 위해
    Context context;

    public AddCookiesInterceptor(Context context) {
        this.context = context;
    }


    // 서버로 리퀘스트를 보내기 전에 가로채서 쿠키 정보를 추가하는 과정
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();


//      로그인 정보를 SharedPreferences 에서 가져온다.
        SharedPreferences pref_login = context.getSharedPreferences(context.getString(R.string.key_pref_Login) , 0);

//        SharedPreferences 에서 쿠키 정보를 꺼낸다. 쿠키는 스트링의 해쉬셋으로 구성되어 있다.
        // 해쉬 셋은 key-value로 구성된 세트다.(중복되지 않는 value 저장)
        Set<String> preferences = pref_login.getStringSet("Cookie" , new HashSet<String>() );


        //리퀘스트 빌더에 쿠키 정보를 추가한다.
        for (String cookie : preferences) {
            builder.addHeader("Cookie", cookie);
        }

        // Web,Android,iOS 구분을 위해 User-Agent세팅
        builder.removeHeader("User-Agent").addHeader("User-Agent", "Android");


        return chain.proceed(builder.build());
    }
}