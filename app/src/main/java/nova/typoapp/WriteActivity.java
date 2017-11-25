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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

        writer = pref_login.getString(getString(R.string.cookie_name) , "");

    }


    @OnClick(R.id.buttonWrite)
    void onWrite() {

        WriteTask writeTask = new WriteTask();
        writeTask.execute();

    }



    public class WriteTask extends AsyncTask<Void, Integer, Boolean> {
        String json_result = "";


        @Override
        protected Boolean doInBackground(Void... unused) {

/* 인풋 파라메터값 생성 */


            String param = "writer=" + writer + "&title=" + editTitle.getText().toString() + "&content=" + editContent.getText().toString();

            boolean success = false;

            try {
/* 서버연결 */
                URL url = new URL(
                        "http://115.68.231.13/project/android/writeFeed.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

/* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

/* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;


                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buff.append(line + "\n");
                }
                json_result = buff.toString().trim();

                //<editor-fold desc="json 파싱 관련파트.">
                // json_result는 결과값으로 가져온 json String이다. json오브젝트에 이 스트링을 담는다.


                JSONObject jsonRes = null;
                try {
                    jsonRes = new JSONObject(json_result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // RECV 데이터에 php에서 뱉은 echo가 들어간다!
                Log.e("RECV DATA", json_result);

                //json을 성공적으로 서버에서 수신했다. 쿠키를 저장시키자
                if (json_result.contains("success")) {

                    success = true;

                }

                //</editor-fold>

                Log.e("success", String.valueOf(success));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            if (success) {
//                Snackbar.make(findViewById(R.id.email_sign_in_button), "환영합니다. 계정"+email+"으로 가입하셨습니다.", Snackbar.LENGTH_LONG).show();


                LauncherActivity.LoginToken = true;
                Toast.makeText(WriteActivity.this, "글을 작성하였습니다.", Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

                finish();
            }
            //로그인이 실패하였다.
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
