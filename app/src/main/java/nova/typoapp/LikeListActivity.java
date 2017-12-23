package nova.typoapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.likeList.LikerContent;
import nova.typoapp.likeList.MyLikerItemRecyclerViewAdapter;
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

public class LikeListActivity extends AppCompatActivity {

    int feedID;



    @BindView(R.id.recyclerViewLikeList)
    RecyclerView recyclerViewLikeList;

    MyLikerItemRecyclerViewAdapter myLikerItemRecyclerViewAdapter = new MyLikerItemRecyclerViewAdapter(LikerContent.ITEMS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_list);
        ButterKnife.bind(this);

        Toolbar toolbarCoComment = (Toolbar) findViewById(R.id.toolbarLikeList);
        setSupportActionBar(toolbarCoComment);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        feedID = getIntent().getIntExtra("feedID",-1);



        CallLikeListTask likeListTask = new CallLikeListTask();
        likeListTask.execute();

    }

    /*
         댓글창 새로고침 태스크

         서버에서 댓글을 불러오고, 댓글 프레그먼트의 리사이클러뷰에 세팅한다.
          */
    public class CallLikeListTask extends AsyncTask<Void, String, Void> {


        Context context = LikeListActivity.this;


        //본래 온프리에서 로딩 표시를 했으나, 충분히 속도가 빠르므로 그냥 두었다.
        //데이터를 더 추가하여 테스트 후 주석된 부분의 삭제 여부를 결정하라
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            asyncDialog.setMessage("덧글을 불러오는 중입니다...");

            // show dialog
//            asyncDialog.show();
        }


        //doInBackground 에서 좋아요한 사람 리스트를 가져오기 위한 http 통신을 수행한다.
        // 통신에는 레트로핏2가 사용됐다.

        String json_result = "";

        @Override
        protected Void doInBackground(Void... voids) {


            //리스트 중복을 막기 위해 아이템리스트를 클리어한다.
            //이렇게 안하면 -> 좋아요 리스트가 계속 뒤에 달라붙음
            LikerContent.ITEMS.clear();

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
            retrofitCall = apiService.getLikeList(feedID);

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


                //리스트 세팅을 시작할 때 리스트 중복을 막기 위해 댓글 리스트를 클리어한다.
                SubCommentContent.clearList();


                //jsonArray 에 담긴 아이템 정보들을 빼내어, 댓글 아이템으로 만들고, 리스트에 추가한다.
                for (int i = 0; i < jsonRes.length(); i++) {

                    //jsonArray 의 데이터를 댓글 아이템 객체에 담는다.
                    JSONObject jObject = jsonRes.getJSONObject(i);  // JSONObject 추출


                    String likerEmail = jObject.getString("likerEmail");
                    String likerName = jObject.getString("likerName");

                    String profileUrl = "";
                    if (!jObject.getString("liker_profile_url").equals("")) {
                        profileUrl = jObject.getString("liker_profile_url");
                    }


                    //아이템 객체에 데이터를 다 담은 후, 아이템을 리스트에 추가한다.
                    LikerContent.LikerItem productLikeItem = new LikerContent.LikerItem(likerName, likerEmail, profileUrl);
                    LikerContent.addItem(productLikeItem);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        //좋아요 리스트를 서버에서 가져와 아이템 리스트에 세팅했다.
        //좋아요 액티비티의 리사이클러뷰를 업데이트한다.
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

//            asyncDialog.dismiss();


//            editTextSubComment.setText("");





            Context context = LikeListActivity.this;

            recyclerViewLikeList.setLayoutManager( new LinearLayoutManager(context) );
            recyclerViewLikeList.setAdapter( myLikerItemRecyclerViewAdapter );



        }

    }

    public void updateRecyclerViewLikeList(){

        myLikerItemRecyclerViewAdapter.notifyDataSetChanged();
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
