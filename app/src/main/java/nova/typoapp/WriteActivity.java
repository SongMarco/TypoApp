package nova.typoapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nova.typoapp.dummy.NewsFeedContent;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

public class WriteActivity extends AppCompatActivity {


    @BindView(R.id.editTitle)
    EditText editTitle;
    @BindView(R.id.editContent)
    EditText editContent;

    String writer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        SharedPreferences pref_login = getSharedPreferences(getString(R.string.key_pref_Login), Activity.MODE_PRIVATE);

        writer = pref_login.getString(getString(R.string.cookie_name), "");

    }


    @OnClick(R.id.buttonWrite)
    void onWrite() {

        WriteTask writeTask = new WriteTask();
        writeTask.execute();

    }
    String json_result = "";
    public class WriteTask extends AsyncTask<Void, String, String> {


        @Override
        protected String doInBackground(Void... voids) {

            //region//글쓰기

            Retrofit retrofit = new Retrofit.Builder().baseUrl(ApiService.API_URL).build();
            ApiService apiService = retrofit.create(ApiService.class);

            Call<ResponseBody> comment = apiService.write(writer, editTitle.getText().toString(), editContent.getText().toString());


            try {

                json_result = comment.execute().body().string();
                return json_result;
            } catch (IOException e) {
                e.printStackTrace();
            }


            //endregion

            return null;
        }


        @Override

        protected void onPostExecute(String result){

            super.onPostExecute(result);

            Log.e("wow", result);


            if ( result.contains("success") ) {
//                Snackbar.make(findViewById(R.id.email_sign_in_button), "환영합니다. 계정"+email+"으로 가입하셨습니다.", Snackbar.LENGTH_LONG).show();


                LauncherActivity.LoginToken = true;
                Toast.makeText(WriteActivity.this, "글을 작성하였습니다.", Toast.LENGTH_SHORT).show();


                NewsFeedContent.called = false;

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

                finish();
            }
            //글쓰기가 실패함
            //에러메시지를 확인하고, 해당 에러를 텍스트뷰에 세팅한다.
            else {

                Toast toast = Toast.makeText(WriteActivity.this, "글 작성에 실패하였습니다.", Toast.LENGTH_SHORT);
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
