package nova.typoapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.LoginInfo;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity {


    @BindView(R.id.profileEmail)
    TextView profileEmail;
    @BindView(R.id.profileName)
    TextView profileName;
    @BindView(R.id.profileBirthday)
    TextView profileBirthday;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LookupSessionTask profileTask = new LookupSessionTask();
        profileTask.execute();





        /////////////////////////////////////////////////////////////////////////



        //,프로필


    }

    String json_result = "";

    String email, name, birthday;
    public class LookupSessionTask extends AsyncTask<Void, String, Void> {


        private static final String TAG = "myTag";

        @Override
        protected Void doInBackground(Void... voids) {



//            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new ReceivedCookiesInterceptor(ProfileActivity.this))
                    .addInterceptor(new AddCookiesInterceptor(ProfileActivity.this))
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


                profileEmail.setText(email);
                profileName.setText(name);
                profileBirthday.setText(birthday);
            }


            //아이디 중복으로 가입이 실패하였다.
            //에러메시지를 확인하고, 해당 에러를 텍스트뷰에 세팅한다.
            else {

                Toast toast = Toast.makeText(ProfileActivity.this, "로그인 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT);
                int offsetX = 0;
                int offsetY = 0;
                toast.setGravity(Gravity.CENTER, offsetX, offsetY);
                toast.show();

            }


        }

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }







}

