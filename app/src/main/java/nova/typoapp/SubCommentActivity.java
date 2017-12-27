package nova.typoapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

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
import nova.typoapp.subcoment.SubCommentContent;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;

public class SubCommentActivity extends AppCompatActivity implements SubCommentFragment.OnListFragmentInteractionListener {


    @BindView(R.id.buttonSendSubComment)
    Button buttonSendSubComment;

    @BindView(R.id.editTextSubComment)
    EditText editTextSubComment;

    //원 댓글 뷰
    @BindView(R.id.subCommentCommentProfileImage)
    ImageView mSubCommentCommentProfileImage;
    @BindView(R.id.subCommentCommentWriter)
    TextView mSubCommentCommentWriter;
    @BindView(R.id.subCommentCommentContent)
    TextView mSubCommentCommentContent;
    @BindView(R.id.subCommentCommentDate)
    TextView mSubCommentCommentDate;


    String textSubCommentContent;

    private static String TAG = "subCommentTag";

    int commentID;


    public void updateSubCommentList() {

        RefreshSubCommentTask refreshSubCommentTask = new RefreshSubCommentTask();
        refreshSubCommentTask.execute();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_comment);
        ButterKnife.bind(this);

        Toolbar toolbarCoComment = (Toolbar) findViewById(R.id.toolbarCoComment);
        setSupportActionBar(toolbarCoComment);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        commentID = getIntent().getIntExtra("commentID", -1);



        //댓글의 데이터를 인텐트에서 가져온다
        Intent gotIntent = getIntent();

        String imgProfileUrl = gotIntent.getStringExtra("imgProfileUrl");
        String commentWriter = gotIntent.getStringExtra("commentWriter");
        String commentContent = gotIntent.getStringExtra("commentContent");
        String commentDate = gotIntent.getStringExtra("commentDate");

//        intent.putExtra("imgProfileUrl", item.imgProfileUrl);
//        intent.putExtra("commentWriter", item.commentWriter);
//        intent.putExtra("commentContent", item.commentContent);
//        intent.putExtra("commentDate", item.commentDate);

        //프로필 이미지를 동그랗게 하기 위한 코드.
        mSubCommentCommentProfileImage.setBackground(new ShapeDrawable(new OvalShape()));
        mSubCommentCommentProfileImage.setClipToOutline(true);

        //답글을 달 댓글의 뷰를 세팅한다.

        //프로필 이미지 세팅하기
        RequestOptions requestOptions = new RequestOptions()
                .error(R.drawable.com_facebook_profile_picture_blank_square);

        Glide.with(SubCommentActivity.this).load(imgProfileUrl)
                .apply(requestOptions)
                .into(mSubCommentCommentProfileImage);

        mSubCommentCommentWriter.setText(commentWriter);

        mSubCommentCommentContent.setText(commentContent);

        mSubCommentCommentDate.setText(commentDate);



    }

    @OnClick(R.id.buttonSendSubComment)
    void sendSubComment() {
        textSubCommentContent = editTextSubComment.getText().toString();


        WriteSubCommentTask writeSubCommentTask = new WriteSubCommentTask();
        writeSubCommentTask.execute();


    }
    // 답글 작성에 필요한 태스크

    /*
    서버로 답글 데이터를 보내고,
    답글이 서버에 등록되면 리사이클러뷰를 리프레쉬 하여 답글을 보여주게 된다.

     */

    public class WriteSubCommentTask extends AsyncTask<Void, String, Void> {

        Context context = SubCommentActivity.this;

        ProgressDialog asyncDialog = new ProgressDialog(
                SubCommentActivity.this);


        //온프리에서 다이얼로그를 띄운다.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("댓글 작성중입니다...");

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
            Call<ResponseBody> retrofitCall;


            //이전 액티비티에서 commentID를 인텐트로 보내줘야 한다.

            //콜 객체를 만든다. 메소드에는 댓글의 ID값, 답글의 내용이 들어간다.
            retrofitCall = apiService.writeSubComment(commentID, textSubCommentContent);

            Log.e(TAG, "commentID = " + commentID + ", content = " + textSubCommentContent);
            try {

                json_result = retrofitCall.execute().body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }


        //작성 완료. 댓글창을 리프레시하고, 로딩 창을 닫아준다.
        //마지막에 댓글 작성자에게 fcm 메시지를 전송한다.
        @Override


        protected void onPostExecute(Void voids) {

            super.onPostExecute(voids);


            //리프레시 태스크 돌릴 것

            //입력을 완료하면 에딧텍스트의 포커스를 해제하고, 키보드를 닫는다.
            editTextSubComment.setText("");
            editTextSubComment.clearFocus();

            InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            Toast.makeText(context, "답글이 등록되었습니다.", Toast.LENGTH_SHORT).show();

            RefreshSubCommentTask refreshSubCommentTask = new RefreshSubCommentTask();
            refreshSubCommentTask.execute();


            asyncDialog.dismiss();


            //댓글 작성자에게 fcm 메시지를 전송한다.


            SendFcmWhenSubCommentToCommentTask subCommentToCommentTask = new SendFcmWhenSubCommentToCommentTask(SubCommentActivity.this);

            subCommentToCommentTask.execute(commentID);


        }
    }

    public class SendFcmWhenSubCommentToCommentTask extends AsyncTask<Integer, String, String> {

        String json_result;
        private Context mContext;

        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.

        public SendFcmWhenSubCommentToCommentTask (Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(Integer... integers) {

            //region//글 삭제하기 - DB상에서 뉴스피드 글을 삭제한다.
            //레트로핏 기초 컴포넌트 만드는 과정. 자주 복붙할 것.
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new ReceivedCookiesInterceptor(mContext))
                    .addInterceptor(new AddCookiesInterceptor(mContext))
                    .addInterceptor(httpLoggingInterceptor)
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(okHttpClient)
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
//            Log.e("myimg", "doInBackground: " + uploadImagePath);


            //좋아요를 한 대상의 타입이다. 여기서는 게시물이므로 feed 라 하였다.
            String type = "feed";

            // 태스크를 만들 때 파라미터로 전송한 feed ID 값이다.
            int feed_ID = integers[0];

            // 레트로핏 콜 객체를 만든다. 파라미터로 게시물의 ID값, 게시물의 타입을 전송한다.
            //todo 좋아요를 자꾸 하면서 장난을 치면 알림을 막는 로직이 필요하다.
            Call<ResponseBody> comment = apiService.likeFeed(feed_ID, type );

            try {

                json_result = comment.execute().body().string();
                return json_result;
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            //게시물에 좋아요를 적용/취소하였다.

        }

    }


    /*
       댓글창 새로고침 태스크

       서버에서 댓글을 불러오고, 댓글 프레그먼트의 리사이클러뷰에 세팅한다.
        */
    public class RefreshSubCommentTask extends AsyncTask<Void, String, Void> {


        Context context = SubCommentActivity.this;


        //본래 온프리에서 로딩 표시를 했으나, 충분히 속도가 빠르므로 그냥 두었다.
        //데이터를 더 추가하여 테스트 후 주석된 부분의 삭제 여부를 결정하라
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //리스트 세팅을 시작할 때 리스트 중복을 막기 위해 댓글 리스트를 클리어한다.
            SubCommentContent.ITEMS.clear();
//            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            asyncDialog.setMessage("덧글을 불러오는 중입니다...");

            // show dialog
//            asyncDialog.show();
        }


        //doInBackground 에서 댓글을 가져오기 위한 http 통신을 수행한다.
        // 통신에는 레트로핏2가 사용됐다.

        String json_result = "";

        @Override
        protected Void doInBackground(Void... voids) {


            //okHttp 클라이언트를 생성한다.
            // 로그 생성을 위해 httpLoggingInterceptor 를 사용했다.
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
            //feedID를 가져온다. (해당 글의 댓글을 가져오기 위함)
            Call<ResponseBody> retrofitCall;
            retrofitCall = apiService.getSubCommentList(commentID);

            //콜 객체를 실행하여, 레트로핏 통신을 실행한다.
            //jsonArray 형식의 데이터가 response 로 들어온다.
//            Log.e(TAG, "textCommentFeed: " + commentID);
            try {

                //결과값을 json_result 에 담는다.
                json_result = retrofitCall.execute().body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }

            JSONArray jsonRes = null;
            try {

                //받아온 결과값을 jsonArray 로 만든다.
                jsonRes = new JSONArray(json_result);




                //jsonArray 에 담긴 아이템 정보들을 빼내어, 댓글 아이템으로 만들고, 리스트에 추가한다.
                for (int i = 0; i < jsonRes.length(); i++) {

                    //jsonArray 의 데이터를 댓글 아이템 객체에 담는다.
                    JSONObject jObject = jsonRes.getJSONObject(i);  // JSONObject 추출

                    int commentID = jObject.getInt("commentID");

                    int subCommentID = jObject.getInt("subCommentID");

                    String writer = jObject.getString("writer");

                    String writerEmail = jObject.getString("writer_email");


                    String content = jObject.getString("text_content");

                    String writtenDate = jObject.getString("written_time");


                    int depth = jObject.getInt("depth");


                    String profileUrl = "";
                    if (!jObject.getString("writer_profile_url").equals("")) {
                        profileUrl = jObject.getString("writer_profile_url");
                    }


                    //아이템 객체에 데이터를 다 담은 후, 아이템을 리스트에 추가한다.
                    SubCommentContent.SubCommentItem productSubComment = new SubCommentContent.SubCommentItem(commentID, subCommentID, depth, writer, writerEmail, content, writtenDate, profileUrl);
                    SubCommentContent.addItem(productSubComment);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            for (CommentContent.CommentItem item : CommentContent.ITEMS) {
                Log.i(TAG, "item writer: " + item.commentWriter + "item content: " + item.commentContent);
            }

            return null;
        }

        //댓글을 서버에서 가져와 리스트에 세팅했다.
        //댓글 프레그먼트의 리사이클러뷰를 업데이트한다.
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

//            asyncDialog.dismiss();




            // 댓글 프래그먼트를 가져와서, updateRecyclerViewComment 메소드를 콜하여 리사이클러뷰를 업데이트 한다.
            SubCommentFragment subCommentFragment = (SubCommentFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentSubComment);

            if (subCommentFragment != null) {

//                Toast.makeText(context, "update called", Toast.LENGTH_SHORT).show();
                subCommentFragment.updateRecyclerView();

            }

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

    @Override
    protected void onResume() {
        super.onResume();

        RefreshSubCommentTask refreshSubCommentTask = new RefreshSubCommentTask();
        refreshSubCommentTask.execute();

    }

    @Override
    public void onListFragmentInteraction(SubCommentContent.SubCommentItem item) {

    }


}
