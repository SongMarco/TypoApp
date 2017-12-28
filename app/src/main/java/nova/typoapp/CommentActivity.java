package nova.typoapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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


/*
CommentActivity

사용자가 게시물을 클릭하였을 때 넘어오는 댓글 작성 화면이다.

화면 상단에는 좋아요 갯수와 좋아요 버튼이 배치되어 있다.

화면 중단에는 댓글이 리사이클러 뷰 형태로 배치되어 있다.

화면 하단에는 댓글 입력창이 있으며 종이비행기 모양의
버튼을 누르면 댓글이 등록된다.

(댓글 입력 - 공백은 예외처리 필요)
 */

public class CommentActivity extends FragmentActivity
implements CommentFragment.OnListFragmentInteractionListener {

    //덧글 화면의 뷰를 세팅 -> onCreate 에서 butterknife 의 bind 메소드를 통해
    //inflate 된다.
    @BindView(R.id.buttonSendComment)
    Button buttonSendComment;

    @BindView(R.id.editTextComment)
    EditText editTextComment;

    @BindView(R.id.parentLayoutComment)
    LinearLayout layoutComment;

    @BindView(R.id.textViewLikeInComment)
    TextView textViewLikeInComment;


    @BindView(R.id.buttonLikeFeedInComment)
    ToggleButton buttonLikeFeedInComment;


    String textCommentContent;


//    public static int getFeedID() {
//        return feedID;
//    }

    // 뉴스피드의 ID번호값이다.
    // 이 변수는 CommentFragment 에서도 사용이 되어, static 변수로 정의했다.
    static int feedID;


    public static String TAG = "commentTag";

    public void updateCommentList(){

        RefreshCommentTask refreshCommentTask = new RefreshCommentTask();
        refreshCommentTask.execute();

    }


    int likeNum;

    String wordName;
    String emailFeedWriter;

    Boolean isLiked;
    /*
    댓글 화면 초기화
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        ButterKnife.bind(this);



        likeNum = getIntent().getIntExtra("likeNum", 0);
        wordName = getIntent().getStringExtra("wordName");
        emailFeedWriter = getIntent().getStringExtra("emailFeedWriter");

        textViewLikeInComment.setText("좋아요 "+likeNum+"개");
        /*
        새로고침 태스크를 실행한다.
        이 태스크는 http 통신 후 서버에서 게시물의 댓글을
        불러와 리사이클러뷰에 세팅한다.

         */
        RefreshCommentTask refreshCommentTask = new RefreshCommentTask();
        refreshCommentTask.execute();


        //리프레시 태스크에서 해당 명령어를 수행하므로, 주석처리 하였다. 추후 오류가 없을시 삭제할 것
//        //리프레시 태스크에서 리사이클러뷰를 다 세팅하면 리스트를 클리어한다.
//        CommentContent.clearList();

        //댓글 쓰려는 글의 ID를 겟인텐트로 가져온다.
        feedID = getIntent().getIntExtra("feedID", 0);

        // fcm 알림을 통해 댓글로 들어왔을 수 있다. 이 때에는 다른 방법으로 feedID를 세팅한다.

        //feedIDFromFcm 세팅 -> 알림을 통해 들어온 것임 -> feedID 새로 세팅
        if( getIntent().getIntExtra("feedIDFromFcm", -1) != -1  ){

            feedID = getIntent().getIntExtra("feedIDFromFcm", -1);
        }

        Log.e(TAG, "onCreate: "+feedID);




        isLiked = getIntent().getBooleanExtra("isLiked", false);
        if(isLiked){
            buttonLikeFeedInComment.setChecked(true);
            buttonLikeFeedInComment.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likegrey, 0, 0, 0);
        }
        else{

        }


           /*
            좋아요 버튼에 setOnCheckedChangeListener 리스너를 세팅
             */

             /*
            체크 상태가 변화하고 이 함수로 들어간다.

            바뀌어서 체크된 상태다 -> 좋아요 적용
            바뀌어서 체크가 안된 상태 -> 좋아요 해제
             */
       buttonLikeFeedInComment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
//                        Toast.makeText(buttonView.getContext(), "좋아요 적용" + item.getFeedID(), Toast.LENGTH_SHORT).show();
                    buttonLikeFeedInComment.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likegrey, 0, 0, 0);

                    likeNum++;
                    textViewLikeInComment.setText("좋아요 "+likeNum+"개");

                    LikeFeedTaskInComment likeFeedTask = new LikeFeedTaskInComment(CommentActivity.this);
                    likeFeedTask.execute(feedID);

                } else { //optional + , your preference on what to to :)

//                        Toast.makeText(buttonView.getContext(), "좋아요 취소" + item.getFeedID(), Toast.LENGTH_SHORT).show();

                    buttonLikeFeedInComment.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likewhite, 0, 0, 0);

                    likeNum--;
                    textViewLikeInComment.setText("좋아요 "+likeNum+"개");


                    LikeFeedTaskInComment likeFeedTask = new LikeFeedTaskInComment(CommentActivity.this);
                    likeFeedTask.execute(feedID);
                }


            }

        });





    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // 인텐트에서 feedIDFromFcm 값이 건져진다면 알림에서 접근한 것이다.
        // 현재 단일 액티비티 상태이므로, 뒤로가기를 누르면 메인 액티비티로 가도록 해주어야 한다.
        if( getIntent().getIntExtra("feedIDFromFcm", -1) != -1 ){

            Intent intent = new Intent(this, MainActivity.class);

            intent.putExtra("feedIDFromFcm", feedID);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);
            finish();
        }


    }

    @OnClick(R.id.buttonSendComment)
    void sendComment(){

        textCommentContent = editTextComment.getText().toString();



        WriteCommentTask writeCommentTask = new WriteCommentTask();
        writeCommentTask.execute();



    }

    @OnClick(R.id.textViewLikeInComment)
    void getLikeList(){

        Intent intent = new Intent( CommentActivity.this, LikeListActivity.class);
        intent.putExtra("feedID",feedID);
//                NewsFeedFragment.isWentCommentActivity = true;
        startActivity(intent);
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


        // 댓글 등록이 완료되었다.
        // 댓글창을 리프레시하고
        // 다이얼로그를 닫아준 뒤 종료한다.
        // 추가 사항 : FCM 메시징으로 좋아요 태스크를 돌린다.
        @Override


        protected void onPostExecute(Void voids) {

            super.onPostExecute(voids);



            //리프레시 태스크 돌릴 것

            RefreshCommentTask refreshCommentTask = new RefreshCommentTask();
            refreshCommentTask.execute();

            asyncDialog.dismiss();

//             뉴스피드 작성자에게 댓글이 작성되었다는 메시지를 전송한다.

            SendFcmWhenCommentFeedTask fcmWhenCommentFeedTask = new SendFcmWhenCommentFeedTask(CommentActivity.this);
            fcmWhenCommentFeedTask.execute();


        }
    }


    /*
    뉴스피드에 댓글을 등록할 때
    Fcm 푸시 알림을 전송하는 태스크다.

    댓글을 등록한 뒤 실행된다.

     */
    public class SendFcmWhenCommentFeedTask extends AsyncTask<Void, String, String> {

        private Context mContext;

        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.

        public SendFcmWhenCommentFeedTask (Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(Void... voids) {

            //region//글 삭제하기 - DB상에서 뉴스피드 글을 삭제한다.
            //레트로핏 기초 컴포넌트 만드는 과정. 자주 복붙할 것.
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    //php 세션ID를 보내기 위한 인터셉터들.
                    .addInterceptor(new ReceivedCookiesInterceptor(mContext))
                    .addInterceptor(new AddCookiesInterceptor(mContext))
                    .addInterceptor(httpLoggingInterceptor)
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(okHttpClient)
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);




            // 서버로 fcm 요청을 보낸다. 세션 ID를 세팅해서 보내므로
            // 세션에서 로그인 정보를 꺼내어 보낼 수 있다.
            Call<ResponseBody> comment = apiService.fcmSendMessageWhenCommentFeed(wordName, emailFeedWriter, feedID);


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



        }

    }













    /*
    댓글창 새로고침 태스크

    서버에서 댓글을 불러오고, 댓글 프레그먼트의 리사이클러뷰에 세팅한다.

    //todo 새로고침 태스크 주의사항 -- 프래그먼트 상에서의 새로고침 태스크는
    당겨서 새로고침 할때 돌아가는 것이고,

    지금 있는 것은 액티비티가 resume 될 때 콜 된다.

    프래그먼트의 새로고침을 잘못 건드려서 망치지 말 것 @@@@@

     */
    public class RefreshCommentTask extends AsyncTask<Void, String, Void> {


        List<CommentContent.CommentItem> productItems = new ArrayList<CommentContent.CommentItem>();
//        Context context = CommentActivity.this;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

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
//                    .addInterceptor(new ReceivedCookiesInterceptor(context))
//                    .addInterceptor(new AddCookiesInterceptor(context))
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
            retrofitCall = apiService.getCommentList(feedID);

            //콜 객체를 실행하여, 레트로핏 통신을 실행한다.
            //jsonArray 형식의 데이터가 response 로 들어온다.
            Log.e(TAG, "textCommentFeed: " + feedID);
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
                    int feedID = jObject.getInt("feedID");

                    String writer = jObject.getString("writer");

                    String writerEmail =  jObject.getString("writer_email");



                    String content = jObject.getString("text_content");

                    String writtenDate = jObject.getString("written_time");

                    int depth = jObject.getInt("depth");

                    int subCommentNum = jObject.getInt("comment_subcomment_num");

                    String profileUrl = "";
                    if (!jObject.getString("writer_profile_url").equals("")) {
                        profileUrl = jObject.getString("writer_profile_url");
                    }


                    //아이템 객체에 데이터를 다 담은 후, 아이템을 리스트에 추가한다.
                    //주의사항 : 리프레시는 글을 작성할 때에는 액티비티, 당겨서 새로고침을 할 때에는 프래그먼트에서 콜 되는 것에 유의하라.
                    //아래 생성자를 고칠 경우, 프래그먼트의 생성자 또한 고쳐야 한다.
                    // 더 좋은 방법은 새로고침 태스크를 스태틱하게 만들어서 어디서든 콜 할 수 있게 만드는 것인데, 아직 적용하지 못했다.
                    CommentContent.CommentItem productComment = new CommentContent.CommentItem(commentID, feedID, depth, subCommentNum, writer, writerEmail , content, writtenDate, profileUrl);
                    productItems.add(productComment);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

//            for (CommentContent.CommentItem item  : CommentContent.ITEMS){
//                Log.i(TAG,"item writer: "+item.commentWriter+ "item content: "+item.commentContent);
//            }

            return null;
        }

        //댓글을 서버에서 가져와 리스트에 세팅했다.
        //댓글 프레그먼트의 리사이클러뷰를 업데이트한다.
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            //리스트 세팅을 시작할 때 리스트 중복을 막기 위해 댓글 리스트를 클리어한다.
            CommentContent.clearList();
            CommentContent.ITEMS.addAll(productItems);

//            asyncDialog.dismiss();


            editTextComment.setText("");


            // 댓글 프래그먼트를 가져와서, updateRecyclerViewComment 메소드를 콜하여 리사이클러뷰를 업데이트 한다.

            CommentFragment commentFragment = (CommentFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentCommentList);
            if(commentFragment != null){

//                Toast.makeText(context, "update called", Toast.LENGTH_SHORT).show();
                commentFragment.updateRecyclerViewComment();

            }

        }

    }



/*
게시물에 좋아요를 적용하는 태스크

feedID를 파라미터로 받아, 서버로 전송하게 된다.

서버에서는 이 feedID를 이용, 좋아요 DB 상에 적용하게 된다.

좋아요를 안 한 게시물이면 좋아요를 적용하며,
좋아요를 한 게시물이라면 좋아요를 취소시킨다.


 */
    String json_result = "";
    public class LikeFeedTaskInComment extends AsyncTask<Integer, String, String> {

        private Context mContext;

        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.

        public LikeFeedTaskInComment (Context context) {
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






    @Override
    public void onListFragmentInteraction(CommentContent.CommentItem item) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        RefreshCommentTask refreshCommentTask = new RefreshCommentTask();
        refreshCommentTask.execute();
    }







}
