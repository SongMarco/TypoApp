package nova.typoapp;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.newsfeed.MyNewsFeedRecyclerViewAdapter;
import nova.typoapp.newsfeed.NewsFeedContent;
import nova.typoapp.newsfeed.NewsFeedContent.FeedItem;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_newsfeed_list, container, false);

        ButterKnife.bind(this, view);


        mSwipeViewNewsFeed.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                RefreshTask refreshTask = new RefreshTask();
                refreshTask.execute();

            }
        });

//        Toast.makeText(getActivity(), "onCreateViewCalled", Toast.LENGTH_SHORT).show();

//
//
//        });
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

//        // save RecyclerView state
//        mBundleRecyclerViewState = new Bundle();
//        Parcelable listState = recyclerViewNewsFeed.getLayoutManager().onSaveInstanceState();
//        mBundleRecyclerViewState.putParcelable(BUNDLE_RECYCLER_LAYOUT, listState);


    }


    @Override
    public void onResume() {
        super.onResume();

//        Toast.makeText(getContext(), "onResume", Toast.LENGTH_SHORT).show();
        RefreshTask refreshTask = new RefreshTask();
        refreshTask.execute();

    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(FeedItem item);
    }

    private static final String BUNDLE_RECYCLER_LAYOUT = "classname.recycler.layout";
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */


    /**
     * This is a method for Fragment.
     * You can do the same in onCreate or onRestoreInstanceState
     */


    //    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//        Toast.makeText(getActivity(), "reload state called", Toast.LENGTH_SHORT).show();
//        if(savedInstanceState != null)
//        {
////            Toast.makeText(getActivity(), "reload state called", Toast.LENGTH_SHORT).show();
//            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
//            recyclerViewNewsFeed.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
//        }
//    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        Toast.makeText(getActivity(), "save state called", Toast.LENGTH_SHORT).show();
//        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerViewNewsFeed.getLayoutManager().onSaveInstanceState());
//    }
//
//    public class OpenTask extends AsyncTask<Void, String, String> {
//
//        final ProgressDialog asyncDialog = new ProgressDialog(
//                getActivity());
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//
//            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            asyncDialog.setMessage("글을 불러오는 중입니다...");
//            // show dialog
//            asyncDialog.show();
//        }
//
//        @Override
//        protected String doInBackground(Void... voids) {
//
//            //region 게시물 리스트 불러오기. 주의사항 - 불러오기 이후 어댑터를 세팅해주어야, 제때 뷰를 반환해준다.
//            // 게시물 리스트 불러오기
//            //댓글을 달러 간 것이 아니라면 새로 불러와라.
//
//            //레트로핏 기초 컴포넌트 만드는 과정. 자주 복붙할 것.
//            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//            OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                    .addInterceptor(new ReceivedCookiesInterceptor(getContext()))
//                    .addInterceptor(new AddCookiesInterceptor(getContext()))
//                    .addInterceptor(httpLoggingInterceptor)
//                    .build();
//
//
//            Retrofit retrofit = new Retrofit.Builder()
//                    .baseUrl(API_URL)
//                    .client(okHttpClient)
//                    .build();
//
//            NewsFeedContent.ITEMS.clear();
//
//            ApiService apiService = retrofit.create(ApiService.class);
////            Log.e("myimg", "doInBackground: " + uploadImagePath);
//            final Call<ResponseBody> retrofitCall;
//
//            retrofitCall = apiService.getFeedList();
//
//
//            try {
//                json_result = retrofitCall.execute().body().string();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//
//            //                                       Log.v("Test", response.body().string());
//
//
////                    Log.v("RECV DATA", json_result);
//
//            JSONArray jsonRes = null;
//            try {
//                jsonRes = new JSONArray(json_result);
//
//                for (int i = 0; i < jsonRes.length(); i++) {
//                    JSONObject jObject = jsonRes.getJSONObject(i);  // JSONObject 추출
//
//                    int feedNum = jObject.getInt("feedNum");
//                    String writer = jObject.getString("writer");
//                    String title = jObject.getString("title");
//                    String content = jObject.getString("text_content");
//                    String writtenDate = jObject.getString("written_time");
//
//                    String writerEmail = jObject.getString("writer_email");
////                            Log.e("hoss", "onResponse: 작성자 email = "+writerEmail );
//
//
//                    String imgUrl = "";
//                    String profileUrl = "";
//
//                    int commentNum = jObject.getInt("comment_num");
//
//                    int likeFeed = jObject.getInt("feed_like");
//
//                    if (!Objects.equals(jObject.getString("imgUrl"), "")) {
//                        imgUrl = jObject.getString("imgUrl");
//                    }
//                    if (!jObject.getString("writer_profile").equals("")) {
//
//                        profileUrl = jObject.getString("writer_profile");
//                    }
//
//                    String isLiked = jObject.getString("is_liked");
//
//                    FeedItem productFeed = new FeedItem(feedNum, likeFeed, isLiked, writer, title, content, imgUrl, profileUrl, writtenDate, commentNum, writerEmail);
//                    NewsFeedContent.addItem(productFeed);
//
//
//                }
//                for (int i = 0; i < NewsFeedContent.ITEMS.size(); i++) {
//                    Log.v("hey", "" + NewsFeedContent.ITEMS.get(i).isLiked);
//                }
//
//
//                if (recyclerViewNewsFeed.getLayoutManager() == null) {
//                    recyclerViewNewsFeed.setLayoutManager(new LinearLayoutManager(getContext()));
//                }
//
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
//
//
////                        for (int i = 0; i < NewsFeedContent.ITEMS.size(); i++) {
////
//////                            Log.e("wow", (i + " : " + NewsFeedContent.ITEMS.get(i).getClass() ) );
//////                            Log.e("wow", (i + " : " + NewsFeedContent.ITEMS.get(i).getInfo() ) );
////
////                        }
////
//
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//
//            //endregion
//
//            return null;
//        }
//
//
//        @Override
//
//        protected void onPostExecute(String result) {
//
//            super.onPostExecute(result);
//            asyncDialog.dismiss();
//            mSwipeViewNewsFeed.setRefreshing(false);
////            Log.e("wow", result);
//
//        }
//
//    }

    public class RefreshTask extends AsyncTask<Void, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            NewsFeedContent.ITEMS.clear();

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

            retrofitCall = apiService.getFeedList();

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
                    NewsFeedContent.addItem(productFeed);


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
                recyclerViewNewsFeed.setAdapter(myNewsFeedRecyclerViewAdapter);
            }


            mSwipeViewNewsFeed.setRefreshing(false);
//            Log.e("wow", result);

        }

    }

}

