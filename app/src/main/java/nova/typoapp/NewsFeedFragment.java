package nova.typoapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.newsfeed.MyNewsFeedRecyclerViewAdapter;
import nova.typoapp.newsfeed.NewsFeedContent;
import nova.typoapp.newsfeed.NewsFeedContent.FeedItem;
import nova.typoapp.notificationlist.NoticeClickedActivity;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.MainActivity.feedIDFromFcm;
import static nova.typoapp.notificationlist.NoticeClickedActivity.feedIDFromFcmInNoticeClicked;
import static nova.typoapp.retrofit.ApiService.API_URL;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class NewsFeedFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;


    private static Bundle mBundleRecyclerViewState;

    //덧글을 달러 갔는지 확인하는 변수이다. 덧글을 달러 갔었다면 -> onCreateView에서
    //리사이클러뷰를 초기화하지 않는다(스크롤이 초기화되면 안되기 때문)
    public static boolean isWentCommentActivity = false;

    //아이템을 삭제했는지 확인하는 변수이다. 아이템을 삭제했다면 화면을 리프레시 해야 하므로,
    //메인 액티비티에서 이를 체크하여 리프레시는 position_none반환(메인 액티비티의 getItemPosition 참조)
    public static boolean isItemDeleted = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewsFeedFragment() {
    }


    public static NewsFeedFragment newInstance(int columnCount) {
        NewsFeedFragment fragment = new NewsFeedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    //리사이클러뷰를 업데이트하라.
    /*
    주의사항. 새로운 레이아웃 매니저를 세팅할 경우,
    스크롤이 초기화되어버린다.

    레이아웃 매니저는 이미 세팅되어있으므로, 다시 세팅하지 않는다.
     */
    public void updateRecyclerView() {


//        recyclerViewNewsFeed.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewNewsFeed.setAdapter(myNewsFeedRecyclerViewAdapter);
//        myNewsFeedRecyclerViewAdapter.notifyDataSetChanged();

    }

    public void getFeedInfo() {


        RefreshOneFeedTask refreshOneFeedTask = new RefreshOneFeedTask();
        refreshOneFeedTask.execute();



    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }




    }


    @BindView(R.id.recyclerViewNewsFeed)
    RecyclerView recyclerViewNewsFeed;

    @BindView(R.id.swipeViewNewsFeed)
    SwipeRefreshLayout mSwipeViewNewsFeed;


    String json_result;


    MyNewsFeedRecyclerViewAdapter myNewsFeedRecyclerViewAdapter = new MyNewsFeedRecyclerViewAdapter(NewsFeedContent.ITEMS, new MyNewsFeedRecyclerViewAdapter.ClickListener() {
        @Override
        public void onPositionClicked(int position) {

        }

        @Override
        public void onLongClicked(int position) {

        }
    });

    public void scrollToTop(){

        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewNewsFeed
                .getLayoutManager();
        layoutManager.scrollToPositionWithOffset(0, 0);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_newsfeed_list, container, false);

        ButterKnife.bind(this, view);


        mSwipeViewNewsFeed.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                MainActivity.feedIDFromFcm = -1;

                RefreshFeedTask refreshFeedTask = new RefreshFeedTask();
                refreshFeedTask.execute();

            }
        });





        // 프래그먼트의 액티비티를 가져와서, 메인일 경우 새로고침을 진행.(게시물을 10 개 가져온다.)
        // 메인이 아니라 알림에서 접근한 경우 그에 맞게 asyncTask를 돌린다.(알림 받은 게시물만 한 개 불러온다.)
        if(getActivity().getClass().getSimpleName().equals( MainActivity.class.getSimpleName() )){

            RefreshFeedTask refreshFeedTask = new RefreshFeedTask();
            refreshFeedTask.execute();
//            Toast.makeText(getContext(), "메인에서 들어옴", Toast.LENGTH_SHORT).show();
        }

        if(  getActivity().getClass().getSimpleName().equals( NoticeClickedActivity.class.getSimpleName() )){

//            Toast.makeText(getContext(), "알림에서 들어옴", Toast.LENGTH_SHORT).show();

        }


//        if(  getActivity().getClass().getSimpleName().equals( ProfileActivity.class.getSimpleName() )){
//
//            Toast.makeText(getContext(), "프로필에서 들어옴", Toast.LENGTH_SHORT).show();
//
////            recyclerViewNewsFeed.setNestedScrollingEnabled(false);
////            recyclerViewNewsFeed.setHasFixedSize(false);
//
//            RefreshFeedTask refreshFeedTask = new RefreshFeedTask();
//            refreshFeedTask.execute();
//
//        }


        //왜 on Create 에서 새로고침을 하는가? onResume에서 하면 어떻게되는데?



        recyclerViewNewsFeed.setItemAnimator(null);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        Toast.makeText(getActivity(), "onAttachCalled", Toast.LENGTH_SHORT).show();
        if (context instanceof OnListFragmentInteractionListener) {


            mListener = (OnListFragmentInteractionListener) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

//        mListener = null;
    }

    @Override
    public void onPause() {
//        Toast.makeText(getActivity(), "onPause", Toast.LENGTH_SHORT).show();
        super.onPause();

        Log.e("refresh", "onPause called" );
//        // save RecyclerView state
//        mBundleRecyclerViewState = new Bundle();
//        Parcelable listState = recyclerViewNewsFeed.getLayoutManager().onSaveInstanceState();
//        mBundleRecyclerViewState.putParcelable(BUNDLE_RECYCLER_LAYOUT, listState);


    }


    @Override
    public void onResume() {
        super.onResume();



    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(FeedItem item);
    }

    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";

    public interface OnFragmentInteractionListener {
    }


    public class RefreshFeedTask extends AsyncTask<Void, String, String> {

        List<FeedItem> productItems = new ArrayList<FeedItem>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(Void... voids) {

            Log.e("refresh", "doInBackground: refresh called");

            //레트로핏 기초 컴포넌트 만드는 과정. 자주 복붙할 것.
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new ReceivedCookiesInterceptor(getContext()))
                    .addInterceptor(new AddCookiesInterceptor(getContext()))
                    .addInterceptor(httpLoggingInterceptor)
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(okHttpClient)
                    .build();


            ApiService apiService = retrofit.create(ApiService.class);
//            Log.e("myimg", "doInBackground: " + uploadImagePath);
            Call<ResponseBody> retrofitCall;

            //자 여기선 가장 최근의 게시물부터 가져올거니까
            // 마지막 피드 넘버를 -1로 세팅해서 보낸다.
            //서버에선 -1을 감지하여,

            //메인 액티비티의 스태틱 변수로 파라미터를 집어넣는다.

            //feedIDFromFcm 값은 알림이 올 떄를 제외하고는 -1이다.
            // 서버에선 -1을 감지할 경우 가장 최근의 10개 게시물을 불러오게 된다.(정상 상황)
            // 알림을 통해 feedIDFromFcm 값이 바뀔 경우 해당 게시물부터 페이징을 하게 된다.(알림 상황)

            //문제점 : 당사자 피드가 나오지 않는다.

            // 새로운 메소드를 만들어  id <= feedNum 으로 적용한다면?
            //구현이 복잡함

            // 보낼 때 feedIDFromFcm + 1로 세팅해서 보낸다면?
            //이게 간단히 구현되고 좋군!
            int feedIDFromFcmPlusOne;
            if(feedIDFromFcm != -1){
                feedIDFromFcmPlusOne = feedIDFromFcm+1;
            }
            else{
                feedIDFromFcmPlusOne = feedIDFromFcm;
            }

            retrofitCall = apiService.getMoreFeed(feedIDFromFcmPlusOne);



            //여기서 새로고침 하지 않고,
            // 당겨서 새로고침시 feedFromFcm을 -1로 리셋한다. 다시 최신 글들을 불러오게 된다.

//            if(MainActivity)
//            feedIDFromFcm = -1;
            // show dialog


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


                    FeedItem productFeed = new FeedItem(feedNum, likeFeed, isLiked, writer, title, content, imgUrl, profileUrl, writtenDate, commentNum, writerEmail);

                    //새로운 아이템 어레이를 만들고, post 에서 카피한다.
                    productItems.add(productFeed);
//                    NewsFeedContent.addItem(productFeed);


                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //endregion

            return null;
        }


        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);


            //게시물 데이터를 서버에서 모두 불러왔다.

            //기존의 게시물 리스트를 비우고, 불러왔던 리스트를 담는다.
            //이렇게 하면 inconsistency 문제를 해결할 수 있다.
            NewsFeedContent.ITEMS.clear();
            NewsFeedContent.ITEMS.addAll(productItems);

// Set the adapter
            if (recyclerViewNewsFeed != null) {
                recyclerViewNewsFeed.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //At this point the layout is complete and the
                        //dimensions of recyclerView and any child views are known.
                    }
                });

                if (recyclerViewNewsFeed.getLayoutManager() == null) {
                    recyclerViewNewsFeed.setLayoutManager(new LinearLayoutManager(getContext()));
                }
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                updateRecyclerView();
//                                // 해당 작업을 처리함
//                            }
//                        });
//                    }
//                }).start();

                if(myNewsFeedRecyclerViewAdapter.getEndlessScrollListener() == null){
                    myNewsFeedRecyclerViewAdapter.setEndlessScrollListener(new MyNewsFeedRecyclerViewAdapter.EndlessScrollListener() {
                        @Override
                        public boolean onLoadMore(int position) {


                            // 아래의 LoadMoreFeedTask 태스크에서 리사이클러뷰를 이어붙이게 된다.
                            // notifyDataSetChanged 를 LoadMoreFeedTask 태스크의 postExecute 에서 해주어야,
                            // 리사이클러뷰가 갱신되어 이어진다.

                            //todo 이 부분을 주석처리하면 페이징이 작동하지 않는다. - 시연할 때 보여주기
                            LoadMoreFeedTask loadMoreFeedTask = new LoadMoreFeedTask();

                            Toast.makeText(getContext(), "페이징 작동", Toast.LENGTH_SHORT).show();
//                            Log.e("paging", "onLoadMore: "+NewsFeedContent.ITEMS.get(position-1).getFeedID() );
                            loadMoreFeedTask.execute( NewsFeedContent.ITEMS.get(position-1).getFeedID() );

                            return false;

                        }
                    });
                }


                recyclerViewNewsFeed.setAdapter(myNewsFeedRecyclerViewAdapter);
            }


            mSwipeViewNewsFeed.setRefreshing(false);
//            Log.e("wow", result);

        }

    }


    // 알림에서 접근했을 때 하나의 게시물만 가져오는 태스크

    public class RefreshOneFeedTask extends AsyncTask<Void, String, String> {

        Context mContext = getContext();
        List<FeedItem> productItems = new ArrayList<FeedItem>();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(Void... voids) {

            //okHttp 클라이언트를 생성한다.
            // 로그 생성을 위해 httpLoggingInterceptor 를 사용했다.
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

            //레트로핏 콜 객체를 만든다.
            //feedID를 가져온다. (해당 글의 댓글을 가져오기 위함)
            Call<ResponseBody> retrofitCall;

            Log.e("feedIDFromFcm", "inTask: "+feedIDFromFcmInNoticeClicked );
            retrofitCall = apiService.getFeedInfo(feedIDFromFcmInNoticeClicked);

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


                    FeedItem productFeed = new FeedItem(feedNum, likeFeed, isLiked, writer, title, content, imgUrl, profileUrl, writtenDate, commentNum, writerEmail);

                    //새로운 아이템 어레이를 만들고, post 에서 카피한다.
                    productItems.add(productFeed);
//                    NewsFeedContent.addItem(productFeed);


                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //endregion

            return null;
        }


        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);


            //게시물 데이터를 서버에서 모두 불러왔다.

            //기존의 게시물 리스트를 비우고, 불러왔던 리스트를 담는다.
            //이렇게 하면 inconsistency 문제를 해결할 수 있다.
            NewsFeedContent.ITEMS.clear();
            NewsFeedContent.ITEMS.addAll(productItems);

// Set the adapter
            if (recyclerViewNewsFeed != null) {
                recyclerViewNewsFeed.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //At this point the layout is complete and the
                        //dimensions of recyclerView and any child views are known.
                    }
                });

                if (recyclerViewNewsFeed.getLayoutManager() == null) {
                    recyclerViewNewsFeed.setLayoutManager(new LinearLayoutManager(getContext()));
                }
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        getActivity().runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                updateRecyclerView();
//                                // 해당 작업을 처리함
//                            }
//                        });
//                    }
//                }).start();


                if(myNewsFeedRecyclerViewAdapter.getEndlessScrollListener() == null){
                    myNewsFeedRecyclerViewAdapter.setEndlessScrollListener(new MyNewsFeedRecyclerViewAdapter.EndlessScrollListener() {
                        @Override
                        public boolean onLoadMore(int position) {


                            // 아래의 LoadMoreFeedTask 태스크에서 리사이클러뷰를 이어붙이게 된다.
                            // notifyDataSetChanged 를 LoadMoreFeedTask 태스크의 postExecute 에서 해주어야,
                            // 리사이클러뷰가 갱신되어 이어진다.
//
//                            //todo 이 부분을 주석처리하면 페이징이 작동하지 않는다. - 시연할 때 보여주기
//                            LoadMoreFeedTask loadMoreFeedTask = new LoadMoreFeedTask();
//
//                            Toast.makeText(getContext(), "페이징 작동", Toast.LENGTH_SHORT).show();
////                            Log.e("paging", "onLoadMore: "+NewsFeedContent.ITEMS.get(position-1).getFeedID() );
//                            loadMoreFeedTask.execute( NewsFeedContent.ITEMS.get(position-1).getFeedID() );

                            return false;

                        }
                    });
                }


                recyclerViewNewsFeed.setAdapter(myNewsFeedRecyclerViewAdapter);
            }


            mSwipeViewNewsFeed.setRefreshing(false);
//            Log.e("wow", result);

        }

    }

    /*

    리사이클러뷰의 특정 위치에 닿으면(예를 들면 맨 밑)

    게시물을 서버에서 더 불러오는 태스크.

    post 에서 notify 를 해주지 않으면 더 이어지지 않는다.


     */
    public class LoadMoreFeedTask extends AsyncTask<Integer, String, String> {

        String json_result;

        private Context context = getContext();

        List<FeedItem> productItems = new ArrayList<FeedItem>();

        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.



        ProgressDialog asyncDialog = new ProgressDialog(context);


        /*
        onPre 에서 로딩 중임을 다이얼로그로 표시해준다.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            asyncDialog.setMessage("단어를 더 불러오는 중입니다...");
//
//            // show dialog
//            asyncDialog.show();

        }

        /*
        서버에서 필요한 아이템들을 가져온다.
         */
        @Override
        protected String doInBackground(Integer... integers) {



            Log.e("refresh", "doInBackground: refresh called");

            //레트로핏 기초 컴포넌트 만드는 과정. 자주 복붙할 것.
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new ReceivedCookiesInterceptor(getContext()))
                    .addInterceptor(new AddCookiesInterceptor(getContext()))
                    .addInterceptor(httpLoggingInterceptor)
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(okHttpClient)
                    .build();


            ApiService apiService = retrofit.create(ApiService.class);
//            Log.e("myimg", "doInBackground: " + uploadImagePath);
            Call<ResponseBody> retrofitCall;

            retrofitCall = apiService.getMoreFeed(integers[0]);

            // show dialog


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


                    FeedItem productFeed = new FeedItem(feedNum, likeFeed, isLiked, writer, title, content, imgUrl, profileUrl, writtenDate, commentNum, writerEmail);

                    //새로운 아이템 어레이를 만들고, post 에서 카피한다.
                    productItems.add(productFeed);
//                    NewsFeedContent.addItem(productFeed);


                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //endregion

            return null;
        }


        /*
        필요한 아이템을 가져왔고, 어댑터에 notify 하여 적용한다.
         */

        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);


            NewsFeedContent.ITEMS.addAll(productItems);


//            asyncDialog.dismiss();

            myNewsFeedRecyclerViewAdapter.notifyDataSetChanged();



        }

    }

}

