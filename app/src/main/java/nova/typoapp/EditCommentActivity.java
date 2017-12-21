package nova.typoapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;

public class EditCommentActivity extends AppCompatActivity {


    int commentID;
    String commentContent;

    @BindView(R.id.editTextEditComment)
    EditText editTextEditComment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_comment);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarEditComment);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        commentID = intent.getIntExtra("commentID", 0);
        commentContent = intent.getStringExtra("content");


        editTextEditComment.setText(commentContent);

        //커서 위치를 에딧텍스트 맨 끝으로 세팅한다.
        editTextEditComment.setSelection(editTextEditComment.getText().length());

        //포커스를 요청하여, 에딧 텍스트에 키패드를 불러온다.
        //참고사항 - manifest의 액티비티 태그에 android:windowSoftInputMode="stateAlwaysVisible" 속성이 있어야
        //requestFocus 했을 때 키패드가 생성된다.
        editTextEditComment.setFocusableInTouchMode(true);
        editTextEditComment.requestFocus();
    }

    @OnClick({R.id.buttonEditComment, R.id.buttonCancelEditComment})
    void onButtonClick(View view){

        switch( view.getId() ){
            case R.id.buttonEditComment:

                commentContent = editTextEditComment.getText().toString();
                EditCommentTask editCommentTask = new EditCommentTask();
                editCommentTask.execute();
                break;


            case  R.id.buttonCancelEditComment:
                onBackPressed();
                break;

        }


    }

    // 댓글 작성에 필요한 태스크

    public class EditCommentTask extends AsyncTask<Void, String, Void> {

        Context context = EditCommentActivity.this;

        //온프리에서 다이얼로그를 띄운다.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();



        }

        //두인백에서 http통신을 수행한다.

        String json_result = "";
        @Override
        protected Void doInBackground(Void... voids) {

            //레트로핏 기초 컴포넌트 만드는 과정. 자주 복붙할 것.
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new ReceivedCookiesInterceptor(context) )
                    .addInterceptor(new AddCookiesInterceptor(context) )
                    .addInterceptor(httpLoggingInterceptor)
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(okHttpClient)
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
//            Log.e("myimg", "doInBackground: " + uploadImagePath);
            Call<ResponseBody> retrofitCall;


            retrofitCall = apiService.editComment(commentID ,commentContent);

//            Log.e(TAG, "textCommentFeed: "+feedID);
            try {

                json_result = retrofitCall.execute().body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override

        protected void onPostExecute(Void voids) {

            super.onPostExecute(voids);

            //새로고침 태스크 돌릴 필요 없음.
            // 글 수정 후 댓글 액티비티로 돌아가면 onResume 에서 새로고침이 되면서,
            // 수정 사항이 적용된다.

            //수정이 적용되었으므로, 이전 화면으로 돌아간다.
            onBackPressed();

        }
    }



    /*
   좌측 상단의 뒤로가기 버튼을 세팅하기 위한 코드
   뒤로가기 버튼을 누르면, 이전 액티비티로 돌아가게 된다.
    */
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
