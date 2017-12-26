package nova.typoapp.firebase;

/**
 * Created by Administrator on 2017-12-26.
 */


import android.content.Context;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
        Context context = getApplicationContext();
        //okhttp client 세팅
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                //컨텍스트를 확보할 수 없으므로, 이메일 정보를 직접 보내겠다. 컨텍스트를 확보해야
                //세션 ID를 쿠키에 세팅해서 보낼 수 있는데... 다른 방법을 생각해봐야 한다.
                .addInterceptor(new AddCookiesInterceptor(MyFireBaseInstanceIDService.this))
                .addInterceptor(new ReceivedCookiesInterceptor(MyFireBaseInstanceIDService.this))
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