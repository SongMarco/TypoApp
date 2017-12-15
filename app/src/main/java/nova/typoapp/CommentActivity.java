package nova.typoapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    @BindView(R.id.parentLayoutComment)
    LinearLayout layoutComment;

    String textCommentContent;

    public static int getFeedID() {
        return feedID;
    }

    static int feedID;


    public static String TAG = "commentTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ButterKnife.bind(this);


        CommentFragment commentFragment = (CommentFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentCommentList);


        RefreshCommentTask refreshCommentTask = new RefreshCommentTask();
        refreshCommentTask.execute();




//시작할 때 댓글 리스트를 클리어하자.
        CommentContent.clearList();

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


            RefreshCommentTask refreshCommentTask = new RefreshCommentTask();
            refreshCommentTask.execute();


            asyncDialog.dismiss();
        }
    }

    public class RefreshCommentTask extends AsyncTask<Void, String, Void> {

        Context context = CommentActivity.this;
//        ProgressDialog asyncDialog = new ProgressDialog(
//                context);



        //온프리에서 다이얼로그를 띄운다.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();




//            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            asyncDialog.setMessage("덧글을 불러오는 중입니다...");

            // show dialog
//            asyncDialog.show();
        }

        //두인백에서 http통신을 수행한다.

        String json_result = "";


        @Override
        protected Void doInBackground(Void... voids) {

            //레트로핏 기초 컴포넌트 만드는 과정. 자주 복붙할 것.
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new ReceivedCookiesInterceptor(context))
                    .addInterceptor(new AddCookiesInterceptor(context))
                    .addInterceptor(httpLoggingInterceptor)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(okHttpClient)
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
//            Log.e("myimg", "doInBackground: " + uploadImagePath);

            //레트로핏 콜 객체를 만든다.
            //feedID를 코멘트액티비티에서 static 변수로 가져온다.
            Call<ResponseBody> retrofitCall;
            retrofitCall = apiService.getCommentList(feedID);

            //콜 객체를 실행하여, 레트로핏 통신을 실행
            Log.e(TAG, "textCommentFeed: " + feedID);
            try {

                json_result = retrofitCall.execute().body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONArray jsonRes = null;
            try {


                jsonRes = new JSONArray(json_result);
                //시작할 때 댓글 리스트를 클리어하자.
                CommentContent.clearList();

                for (int i = 0; i < jsonRes.length(); i++) {
                    JSONObject jObject = jsonRes.getJSONObject(i);  // JSONObject 추출

                    //jObject에 담긴 아이템 정보들을 빼낸다
                    int commentID = jObject.getInt("commentID");
                    int feedID = jObject.getInt("feedID");

                    String writer = jObject.getString("writer");

                    String content = jObject.getString("text_content");

                    String writtenDate = jObject.getString("written_time");

                    int depth = jObject.getInt("depth");

                    String profileUrl = "";
                    if (!jObject.getString("writer_profile_url").equals("")) {
                        profileUrl = jObject.getString("writer_profile_url");
                    }


                    CommentContent.CommentItem productComment = new CommentContent.CommentItem(commentID, feedID, depth, writer, content, writtenDate, profileUrl);
                    CommentContent.addItem(productComment);



                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            for (CommentContent.CommentItem item  : CommentContent.ITEMS){
                Log.i(TAG,"item writer: "+item.commentWriter+ "item content: "+item.commentContent);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

//            asyncDialog.dismiss();


//// Reload current fragment

            CommentFragment commentFragment = (CommentFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentCommentList);


            if(commentFragment != null){


                Toast.makeText(context, "update called", Toast.LENGTH_SHORT).show();
                commentFragment.updateRecyclerView();

            }


//
//            CommentFragment commentFrag = (CommentFragment)
//                    getSupportFragmentManager().findFragmentById(R.id.fragmentCommentList);
//
////            //commentFrag를 불러와서 업데이트하게 하자
////            if(commentFrag != null){
////                commentFrag.updateRecyclerView();
////            }





        }

    }



    @Override
    public void onListFragmentInteraction(CommentContent.CommentItem item) {

    }
}
