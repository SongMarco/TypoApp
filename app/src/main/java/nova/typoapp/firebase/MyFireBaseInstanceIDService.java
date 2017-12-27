package nova.typoapp.firebase;

/**
 * Created by Administrator on 2017-12-26.
 */


import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import nova.typoapp.retrofit.ApiService;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;





// 토큰을 생성해주는 서비스다.

/*
앱을 새로 설치하고 시작하면 매니페스트에 등록한 서비스에 의해,
자동으로 onTokenRefresh 가 수행된다.

1. 앱을 완전 처음 시작할 때(로그인을 안한 상태)
getToken 메소드에서 기기의 토큰이 발급되며,
이 토큰은 sendRegistrationToServer 를 통해 서버로 전송된다.

토큰은 서버에서 토큰 DB에 된다.
따라서 기기 정보가 서버에 등록이 된다.

2. 로그인을 할 경우

토큰 DB에는 토큰 정보와 사용자 계정 정보(이메일)가 저장되는데,
로그인을 하지 않아서 1번 과정에서는 아직 토큰만 저장해뒀다.

따라서 로그인 할 때 토큰 DB를 업데이트시켜야 한다.

로그인을 계속한다면
토큰은 기기에 따른 고유한 값이므로 변하지 않지만,
이메일은 계정에 따라 변하므로 계속해서 업데이트된다.


 */
public class MyFireBaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";

    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + token);

        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.

        //okhttp client 세팅
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()

//                .addInterceptor(new AddCookiesInterceptor(getApplicationContext()))
//                .addInterceptor(new ReceivedCookiesInterceptor(getApplicationContext()))
                .addInterceptor(httpLoggingInterceptor)
                .build();

        //레트로핏 객체 세팅
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiService.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        ApiService apiService = retrofit.create(ApiService.class);


//       String email = getSharedPreferences(getString(R.string.key_pref_Login), 0).getString("cookie_email", "error");

        Call<ResponseBody> call = apiService.registerToken(token);


        String jsonResult;
        try {
            jsonResult = call.execute().body().string();
            Log.e(TAG, "sendRegistrationToServer: jsonResult ="+jsonResult );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}