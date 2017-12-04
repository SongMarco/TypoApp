package nova.typoapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.LoginInfo;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LauncherActivity extends AppCompatActivity {




    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //셰어드에서 로그인토큰을 가져온다.
        SharedPreferences pref_login = getSharedPreferences(getString(R.string.key_pref_Login) , 0);


        Set<String> preferences = pref_login.getStringSet("Cookie" , new HashSet<String>() );



        setContentView(R.layout.activity_launcher);


        //페이스북 로그인이 된 상태에서 앱을 켰다. 메인 액티비티로 보낸다.
        if (AccessToken.getCurrentAccessToken() != null) {


            intent = new Intent(LauncherActivity.this, MainActivity.class);
            startActivity(intent);

            finish();

        }
        //일반 로그인이 된 상태에서 앱을 켰다.
        // 세션 정보를 확인하고 로그인 처리해주자.

        else if( !preferences.isEmpty() ){

            LookupSessionTask  task = new LookupSessionTask();
            task.execute();

            intent = new Intent(LauncherActivity.this, MainActivity.class);
            startActivity(intent);

            finish();

        }

        //페이스북 / 일반 로그인 안 된 상태. 로그인시킨다.
        else{

            intent = new Intent(LauncherActivity.this, LoginActivity.class);
            startActivity(intent);

            finish();

        }



    }

    String email, name, birthday;
    public class LookupSessionTask extends AsyncTask<Void, String, Void> {


        private static final String TAG = "myTag";

        @Override
        protected Void doInBackground(Void... voids) {



//            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new ReceivedCookiesInterceptor(LauncherActivity.this))
                    .addInterceptor(new AddCookiesInterceptor(LauncherActivity.this))
//                    .addInterceptor(interceptor)
                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiService.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();


            Log.e(TAG, "shared-before call: "+getSharedPreferences("pref_login" , MODE_PRIVATE ).getAll()  ) ;

            ApiService apiService = retrofit.create(ApiService.class);

            Call<LoginInfo> call = apiService.lookSession();

            try {

                LoginInfo loginInfo = call.execute().body();

                Log.e(TAG, "shared-after call: "+getSharedPreferences("pref_login" , MODE_PRIVATE ).getAll()  ) ;
//                String cookie = call.clone().execute().headers().values("Set-Cookie").toString();


                email = loginInfo.getEmail();
                name = loginInfo.getName();
                birthday = loginInfo.getBirthday();



            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {

            super.onPostExecute(voids);


            if ( email!=null ) {
                Toast.makeText(LauncherActivity.this, email+"계정으로 자동 로그인되었습니다.", Toast.LENGTH_SHORT).show();

            }


            //아이디 중복으로 가입이 실패하였다.
            //에러메시지를 확인하고, 해당 에러를 텍스트뷰에 세팅한다.
            else {

                Toast toast = Toast.makeText(LauncherActivity.this, "로그인 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT);
                int offsetX = 0;
                int offsetY = 0;
                toast.setGravity(Gravity.CENTER, offsetX, offsetY);
                toast.show();

            }


        }

    }
}
