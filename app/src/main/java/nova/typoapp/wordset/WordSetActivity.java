package nova.typoapp.wordset;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nova.typoapp.R;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import nova.typoapp.worditem.MyWordItemRecyclerViewAdapter;
import nova.typoapp.worditem.WordCardActivity;
import nova.typoapp.worditem.WordItemContent;
import nova.typoapp.worditem.WordItemContent.WordItem;
import nova.typoapp.wordpuzzle.WordPuzzleActivity;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;
import static nova.typoapp.worditem.WordItemContent.ITEMS;

public class WordSetActivity extends AppCompatActivity {


    private static final String TAG = "WordSetActivity" ;



    int numWords;

    int idWordSet;

    String nameSetOwner;

    String titleWordSet;
    String UrlOwnerProfileImg;

    MyWordItemRecyclerViewAdapter rvAdapterWordItem = new MyWordItemRecyclerViewAdapter(ITEMS);





    @BindView(R.id.tvNumWordsInActivity)
    TextView tvNumWordsInActivity;

    @BindView(R.id.tvNameSetOwnerInActivity)
    TextView tvNameSetOwnerInActivity;
    @BindView(R.id.tvTitleSetInActivity)
    TextView tvTitleSetInActivity;

    @BindView(R.id.imgSetOwnerProfileInActivity)
    ImageView imgSetOwnerProfileInActivity;

    @BindView(R.id.rvWordCardList)
    RecyclerView rvWordCardList;

    @BindView(R.id.cvWordCard)
    CardView cvWordCard;

    @BindView(R.id.cvWordPuzzle)
    CardView cvWordPuzzle;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_set);

        ButterKnife.bind(this);


        //상단 툴바를 세팅한다.
        Toolbar toolbarComment = (Toolbar) findViewById(R.id.toolbarWordSet);

        setSupportActionBar(toolbarComment);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("단어장");



        //리사이클러뷰의 스크롤 기능 해제 - NestedScrollView 안에 있으므로.
        rvWordCardList.setNestedScrollingEnabled(false);

        //인텐트에서 단어장 정보를 가져와 세팅한다

        numWords = getIntent().getIntExtra("numWords", -1);
        nameSetOwner = getIntent().getStringExtra("nameWordSetOwner");
        titleWordSet = getIntent().getStringExtra("nameWordSet");

        idWordSet = getIntent().getIntExtra("idWordSet", -1);
//        Log.e(TAG, "onCreate: "+idWordSet );

//        UrlOwnerProfileImg = getIntent().getStringExtra("UrlOwnerProfileImg");




        //툴바 아래의, 단어장 정보를 세팅한다.
        tvNumWordsInActivity.setText(numWords + " 단어");
        tvNameSetOwnerInActivity.setText(nameSetOwner);
        tvTitleSetInActivity.setText(titleWordSet);

        imgSetOwnerProfileInActivity.setBackground(new ShapeDrawable(new OvalShape()));
        imgSetOwnerProfileInActivity.setClipToOutline(true);

        //단어장 프로필 이미지를 세팅하는데, 인텐트 번들에 프로필 이미지 url 이 없다면 기본 이미지를 세팅한다.
        if ( getIntent().getStringExtra("UrlOwnerProfileImg") != null && !getIntent().getStringExtra("UrlOwnerProfileImg").equals("") ) {
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(this)
                    .load(getIntent().getStringExtra("UrlOwnerProfileImg"))
                    .apply(requestOptions)
                    .into(imgSetOwnerProfileInActivity);
        }


        GetWordListTask getWordListTask = new GetWordListTask();
        getWordListTask.execute();


    }

    public int getIdWordSet(){

        return idWordSet;
    }


    //단어 카드를 보러가자
    @OnClick(R.id.cvWordCard)
    void onClickCard(){

//        Toast.makeText(this, "단어 카드를 보러갑니다", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, WordCardActivity.class);

        //어떤 데이터를 번들로?

        //단어장 데이터를 번들로 보내라. - 어레이 리스트!

        ArrayList<WordItem> bundleItems = new ArrayList<WordItem>();

        bundleItems.addAll(WordItemContent.ITEMS);

        intent.putParcelableArrayListExtra("bundleItems", bundleItems);

        startActivity(intent);
    }

    //단어 카드를 보러가자
    @OnClick(R.id.cvWordPuzzle)
    void onClickPuzzle(){

//        Toast.makeText(this, "단어 카드를 보러갑니다", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, WordPuzzleActivity.class);

        //어떤 데이터를 번들로?

        //단어장 데이터를 번들로 보내라. - 어레이 리스트!

        ArrayList<WordItem> bundleItems = new ArrayList<WordItem>();

        bundleItems.addAll(WordItemContent.ITEMS);

        intent.putParcelableArrayListExtra("bundleItems", bundleItems);

        startActivity(intent);
    }


//
//    // 스캔 라이브러리 액티비티에서 돌아왔다.
//    // 반듯해진 사진을 이미지 뷰에 세팅하여 작업을 마치게 된다.
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // 리퀘스트 코드, 액티비티에서 반환한 결과값이 정상이면 이미지뷰 세팅
//        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//
//
//            // uri 를 번들에서 가져온다.
//            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
//
//            // 문자 인식 액티비티로 이동하여, 문자 인식을 준비한다.
//            Intent intent = new Intent(this , AddWordSetOCRActivity.class);
//
//            intent.putExtra("imgUri", uri);
//
//            startActivity(intent);
//
//        }
//
//    }
    @Override
    protected void onResume() {
        super.onResume();





    }


    public void updateWordList(){
        GetWordListTask getWordListTask = new GetWordListTask();
        getWordListTask.execute();


    }



    public class GetWordListTask extends AsyncTask<Void, String, String> {

        String json_result;
        private Context mContext = WordSetActivity.this;

        List<WordItem> productItems = new ArrayList<>();

        @Override
        protected String doInBackground(Void... integers) {

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


            // 레트로핏 콜 객체를 만든다. 파라미터로 게시물의 ID값, 게시물의 타입을 전송한다.

            Call<ResponseBody> comment = apiService.getWordListFromSet( idWordSet );

            try {
                json_result = comment.execute().body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }


            JSONArray jsonRes = null;
            try {

                //받아온 결과값을 jsonArray 로 만든다.
                jsonRes = new JSONArray(json_result);


                //jsonArray 에 담긴 아이템 정보들을 빼내어, 댓글 아이템으로 만들고, 리스트에 추가한다.
                for (int i = 0; i < jsonRes.length(); i++) {
//                    $arrayFeed = array (
//                            "id_word" => $row ['id_word_feed'],
//
//                            "name_word" => urlencode( $row ['name_word']),
//
//                            "img_url"=>urlencode($img_url),
//                            "mean_word"=> urlencode($mean_word)
//
//    );


                    //jsonArray 의 데이터를 댓글 아이템 객체에 담는다.
                    JSONObject jObject = jsonRes.getJSONObject(i);  // JSONObject 추출

                    int idWord = jObject.getInt("id_word");

                    String nameWord = jObject.getString("name_word");


                    String meanWord = jObject.getString("mean_word");

                    String urlWordImg = "";
                    if (!jObject.getString("img_url").equals("")) {
                        urlWordImg = jObject.getString("img_url");
                    }

                    //아이템 객체에 데이터를 다 담은 후, 아이템을 리스트에 추가한다.

//                    Log.e(TAG, "doInBackground: " + nameSet + numSetWords + nameSetOwner);

                    WordItem productWordItem = new WordItem(idWord, urlWordImg, nameWord, meanWord);

                    productItems.add(productWordItem);

//                    for(int j = 0; j<ITEMS.size(); j++){
//
//                        Log.e(TAG, "onPostExecute: "+productItems.get(i).nameWordSetOwner);
//
//                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);


            //기존의 리스트를 클리어, 리스트를 업데이트한다.
            ITEMS.clear();
            ITEMS.addAll(productItems);

            //단어 갯수도 업데이트!

            int numWords = ITEMS.size();
            tvNumWordsInActivity.setText(numWords + " 단어");


            for (int j = 0; j < ITEMS.size(); j++) {

                WordItem item = ITEMS.get(j);
                Log.e(TAG, "onPostExecute: " + item.nameWord + item.meanWord);

            }

            rvWordCardList.setLayoutManager(new LinearLayoutManager(WordSetActivity.this));

            rvWordCardList.setAdapter(rvAdapterWordItem);
            rvAdapterWordItem.notifyDataSetChanged();


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
