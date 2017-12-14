package nova.typoapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nova.typoapp.comment.CommentContent;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;

public class CommentActivity extends FragmentActivity
implements CommentFragment.OnListFragmentInteractionListener {

    @BindView(R.id.buttonSendComment)
    Button buttonSendComment;

    @BindView(R.id.editTextComment)
    EditText editTextComment;

    String textCommentContent;
    int feedID;


    public static String TAG = "commentTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ButterKnife.bind(this);


        //댓글 쓰려는 글의 ID를 겟인텐트로 가져온다.
        feedID = getIntent().getIntExtra("feedID", 0);
        Log.e(TAG, "onCreate: "+feedID);
    }

    @OnClick(R.id.buttonSendComment)
    void sendComment(){

        textCommentContent = editTextComment.getText().toString();



        WriteCommentTask writeCommentTask = new WriteCommentTask();
        writeCommentTask.execute();



    }

    //댓글을 불러오는 태스크
    // 댓글 작성에 필요한 태스크

    public class CallCommentTask extends AsyncTask<Void, String, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(
                CommentActivity.this);


        //온프리에서 다이얼로그를 띄운다.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("댓글을 불러오는 중입니다...");

            // show dialog
            asyncDialog.show();
        }

        //두인백에서 http통신을 수행한다.

        @Override
        protected Void doInBackground(Void... voids) {

            //레트로핏 기초 컴포넌트 만드는 과정. 자주 복붙할 것.
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new ReceivedCookiesInterceptor(CommentActivity.this))
                    .addInterceptor(new AddCookiesInterceptor(CommentActivity.this))
                    .addInterceptor(httpLoggingInterceptor)
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(okHttpClient)
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
//            Log.e("myimg", "doInBackground: " + uploadImagePath);



            //레트로핏 콜을 만든다.
            Call<ResponseBody> retrofitCall;


            retrofitCall = apiService.writeComment(feedID ,textCommentContent);

            Log.e(TAG, "textCommentFeed: "+feedID);
            try {

                retrofitCall.execute();

            } catch (IOException e) {
                e.printStackTrace();
            }



            return null;
        }


        //작성 완료. 다이얼로그 닫아주고 댓글창 리프레시하고 종료
        @Override


        protected void onPostExecute(Void voids) {

            super.onPostExecute(voids);



            //리프레시 태스크 돌릴 것
            asyncDialog.dismiss();
        }
    }



    // 댓글 작성에 필요한 태스크

    public class WriteCommentTask extends AsyncTask<Void, String, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(
                CommentActivity.this);


        //온프리에서 다이얼로그를 띄운다.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("덧글 작성중입니다...");

            // show dialog
            asyncDialog.show();
        }

        //두인백에서 http통신을 수행한다.

        String json_result = "";
        @Override
        protected Void doInBackground(Void... voids) {

            //레트로핏 기초 컴포넌트 만드는 과정. 자주 복붙할 것.
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new ReceivedCookiesInterceptor(CommentActivity.this))
                    .addInterceptor(new AddCookiesInterceptor(CommentActivity.this))
                    .addInterceptor(httpLoggingInterceptor)
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(okHttpClient)
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
//            Log.e("myimg", "doInBackground: " + uploadImagePath);
            Call<ResponseBody> retrofitCall;


            retrofitCall = apiService.writeComment(feedID ,textCommentContent);

            Log.e(TAG, "textCommentFeed: "+feedID);
            try {

                json_result = retrofitCall.execute().body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }



            return null;
        }


        //작성 완료. 다이얼로그 닫아주고 댓글창 리프레시하고 종료
        @Override


        protected void onPostExecute(Void voids) {

            super.onPostExecute(voids);



            //리프레시 태스크 돌릴 것
            asyncDialog.dismiss();
        }
    }

    public class RefreshCommentTask extends AsyncTask<Void, String, Void> {

//        ProgressDialog asyncDialog = new ProgressDialog(
//                CommentActivity.this);


        //온프리에서 다이얼로그를 띄운다.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            asyncDialog.setMessage("덧글 작성중입니다...");

            // show dialog
//            asyncDialog.show();


        }

        //두인백에서 http통신을 수행한다.

        @Override
        protected Void doInBackground(Void... voids) {


            return null;
        }


        //작성 완료. 다이얼로그 닫아주고 댓글창 리프레시하고 종료
        @Override


        protected void onPostExecute(Void voids) {
            super.onPostExecute(voids);



//            asyncDialog.dismiss();
        }
    }


    @Override
    public void onListFragmentInteraction(CommentContent.CommentItem item) {

    }
}
