package nova.typoapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.AccessToken;

public class LauncherActivity extends AppCompatActivity {
    public static Boolean LoginToken;



    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //셰어드에서 로그인토큰을 가져온다.
        SharedPreferences pref_login = getSharedPreferences(getString(R.string.key_pref_Login), Activity.MODE_PRIVATE);

        LoginToken = pref_login.getBoolean(getString(R.string.LoginToken), false);

        setContentView(R.layout.activity_launcher);


        //페이스북 로그인이 된 상태에서 앱을 켰다. 메인 액티비티로 보낸다.
        if (AccessToken.getCurrentAccessToken() != null) {


            intent = new Intent(LauncherActivity.this, MainActivity.class);
            startActivity(intent);

            finish();

        }
        //일반 로그인이 된 상태에서 앱을 켰다.
        // 세션 정보를 확인하고 로그인 처리해주자.

        else if(LoginToken!=null && LoginToken){

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
}
