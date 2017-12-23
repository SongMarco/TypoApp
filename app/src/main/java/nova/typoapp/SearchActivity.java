package nova.typoapp;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.newsfeed.SearchFeedContent;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;

public class SearchActivity extends AppCompatActivity implements  SearchWordFragment.OnFragmentInteractionListener{


    private static Bundle mBundleSavedWord;


    @BindView(R.id.editTextSearch)
    EditText editTextSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        Toolbar toolbarCoComment = (Toolbar) findViewById(R.id.toolbarSearch);
        setSupportActionBar(toolbarCoComment);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        editTextSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchWord = v.getText().toString();

                    SearchTask searchTask = new SearchTask();
                    searchTask.execute( searchWord );


//                    Toast.makeText(SearchActivity.this, v.getText()+"검색합니다", Toast.LENGTH_SHORT).show();


                    return true;
                }
                return false;
            }
        });

        //포커스를 요청하여, 에딧 텍스트에 키패드를 불러온다.
        //참고사항 - manifest의 액티비티 태그에 android:windowSoftInputMode="stateAlwaysVisible" 속성이 있어야
        //requestFocus 했을 때 키패드가 생성된다.
        editTextSearch.setFocusableInTouchMode(true);
        editTextSearch.requestFocus();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    /*
          댓글창 새로고침 태스크

          서버에서 댓글을 불러오고, 댓글 프레그먼트의 리사이클러뷰에 세팅한다.
           */
    public class SearchTask extends AsyncTask<String, String, Void> {


        Context context = SearchActivity.this;


        //본래 온프리에서 로딩 표시를 했으나, 충분히 속도가 빠르므로 그냥 두었다.
        //데이터를 더 추가하여 테스트 후 주석된 부분의 삭제 여부를 결정하라
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            SearchFeedContent.ITEMS_SEARCH.clear();

//            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            asyncDialog.setMessage("덧글을 불러오는 중입니다...");

            // show dialog
//            asyncDialog.show();
        }


        //doInBackground 에서 댓글을 가져오기 위한 http 통신을 수행한다.
        // 통신에는 레트로핏2가 사용됐다.

        String json_result = "";

        @Override
        protected Void doInBackground(String... strings) {


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
            retrofitCall = apiService.searchFeed( strings[0] );

            //콜 객체를 실행하여, 레트로핏 통신을 실행한다.
            //jsonArray 형식의 데이터가 response 로 들어온다.
//            Log.e(TAG, "textCommentFeed: " + commentID);

            try {
                json_result = retrofitCall.execute().body().string();
                JSONArray jsonRes = null;

                jsonRes = new JSONArray(json_result);

                for (int i = 0; i < jsonRes.length(); i++) {
                    JSONObject jObject = jsonRes.getJSONObject(i);  // JSONObject 추출
                    int feedNum = jObject.getInt("feedNum");
                    String writer = jObject.getString("writer");
                    String title = jObject.getString("title");
                    String content = jObject.getString("text_content");
                    String writtenDate = jObject.getString("written_time");

                    String writerEmail = jObject.getString("writer_email");
//                            Log.e("hoss", "onResponse: 작성자 email = "+writerEmail );


                    String imgUrl = "";
                    String profileUrl = "";

                    int commentNum = jObject.getInt("comment_num");

                    int likeFeed = jObject.getInt("feed_like");

                    if (!Objects.equals(jObject.getString("imgUrl"), "")) {
                        imgUrl = jObject.getString("imgUrl");
                    }
                    if (!jObject.getString("writer_profile").equals("")) {

                        profileUrl = jObject.getString("writer_profile");
                    }

                    String isLiked = jObject.getString("is_liked");

//                    Log.e("hoss", "onResponse: 작성자 email = "+writerEmail );
//                    Log.e("myCommentNum", "onResponse: " + commentNum);
//                            Log.v("hey", writer+title+content);

//                            FeedItem productFeed = NewsFeedContent.createFeed4(writer, title, content, imgUrl);
//                                FeedItem productFeed = NewsFeedContent.createFeed7(feedNum, writer, title, content, imgUrl, profileUrl, writtenDate);
                    SearchFeedContent.FeedItem productSearchFeed = new SearchFeedContent.FeedItem(feedNum, likeFeed, isLiked, writer, title, content, imgUrl, profileUrl, writtenDate, commentNum, writerEmail);
                    SearchFeedContent.addItem(productSearchFeed);


                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //endregion

            return null;
        }

            //댓글을 서버에서 가져와 리스트에 세팅했다.
        //댓글 프레그먼트의 리사이클러뷰를 업데이트한다.
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

//            asyncDialog.dismiss();


            // 댓글 프래그먼트를 가져와서, updateRecyclerViewComment 메소드를 콜하여 리사이클러뷰를 업데이트 한다.
            SearchWordFragment searchWordFragment = (SearchWordFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentSearchWord);

            if (searchWordFragment != null) {

//                Toast.makeText(context, "update called", Toast.LENGTH_SHORT).show();
                searchWordFragment.updateRecyclerViewSearchWord();

            }

        }

    }


    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);


    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);


    }


    @Override
    protected void onPause() {
        super.onPause();

//        Toast.makeText(this, "검색 저장", Toast.LENGTH_SHORT).show();

        // 검색어를 저장한다
        mBundleSavedWord = new Bundle();

        String searchWord = editTextSearch.getText().toString();
        mBundleSavedWord.putString("searchWord", searchWord);





    }

    @Override
    protected void onResume() {
        super.onResume();



        // 검색어를 복원한다.
        if (mBundleSavedWord != null) {
//            Toast.makeText(this, "검색 복원", Toast.LENGTH_SHORT).show();
            String searchWord = mBundleSavedWord.getString("searchWord", "");

            editTextSearch.setText( searchWord );
            //커서 위치를 에딧텍스트 맨 끝으로 세팅한다.
            editTextSearch.setSelection( editTextSearch.getText().length() );



            //검색을 한번 더하여 리프레쉬 시킨다.(좋아요 등의 상호작용을 갱신시키기 위함)
            SearchTask searchTask = new SearchTask();
            searchTask.execute( searchWord );

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
