package nova.typoapp.notificationlist;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.R;
import nova.typoapp.notificationlist.NoticeContent.NoticeItem;
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
public class NoticeItemFragment extends Fragment {


    private static final String ARG_COLUMN_COUNT = "column-count";

    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoticeItemFragment() {
    }








    @SuppressWarnings("unused")
    public static NoticeItemFragment newInstance(int columnCount) {
        NoticeItemFragment fragment = new NoticeItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }



    MyNoticeItemRecyclerViewAdapter myNoticeItemRecyclerViewAdapter = new MyNoticeItemRecyclerViewAdapter(NoticeContent.ITEMS);

    @BindView(R.id.swipeLayoutNotice)
    SwipeRefreshLayout mSwipeViewNotice;

    @BindView(R.id.recyclerViewNotice)
    RecyclerView recyclerViewNotice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice_list, container, false);
        ButterKnife.bind(this, view);


        mSwipeViewNotice.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {



                RefreshNoticeTask refreshNoticeTask = new RefreshNoticeTask();
                refreshNoticeTask.execute();

            }
        });


        RefreshNoticeTask refreshNoticeTask = new RefreshNoticeTask();
        refreshNoticeTask.execute();
//        Toast.makeText(getActivity(), "onCreateViewCalled", Toast.LENGTH_SHORT).show();

//
//
//        });
        recyclerViewNotice.setItemAnimator(null);



        return view;
    }



    public class RefreshNoticeTask extends AsyncTask<Void, String, String> {

        String json_result;

        List<NoticeContent.NoticeItem> productItems = new ArrayList<NoticeContent.NoticeItem>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected String doInBackground(Void... voids) {

//            Log.e("refresh", "doInBackground: refresh called");

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


            retrofitCall = apiService.getNoticeList();

            try {
                json_result = retrofitCall.execute().body().string();
                JSONArray jsonRes = null;

                jsonRes = new JSONArray(json_result);

                for (int i = 0; i < jsonRes.length(); i++) {
                    JSONObject jObject = jsonRes.getJSONObject(i);  // JSONObject 추출


                    int noticeID = jObject.getInt("notice_id");



                    //feedID와 commentID가 ""로 도착했을 때를 걸러내고,
                    //원하는 int 값이 세팅되게 한다.
                    int feedID = -1;
                    if( !jObject.getString("feed_id").equals("")  ){
                        feedID = jObject.getInt("feed_id");
                    }

                    int commentID = -1;
                    if( !jObject.getString("comment_id").equals("")   ){
                        commentID = jObject.getInt("comment_id");
                    }






                    String ownerEmail = jObject.getString("notice_owner_email");
                    String content = jObject.getString("notice_content");

                    String noticeDate = jObject.getString("notice_date");

                    String toWhereActivity = jObject.getString("to_where_activity");

                    String profileUrl = "";
                    if (!jObject.getString("notice_sender_profile_url").equals("")) {

                        profileUrl = jObject.getString("notice_sender_profile_url");
                    }






                    NoticeItem productNotice = new NoticeItem(noticeID, feedID, commentID, ownerEmail, content, noticeDate, toWhereActivity, profileUrl );

                    //새로운 아이템 어레이를 만들고, post 에서 카피한다.
                    productItems.add(productNotice);

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


            //알림 목록을 서버에서 모두 불러왔다.

            //기존의 게시물 리스트를 비우고, 불러왔던 리스트를 담는다.
            //이렇게 하면 inconsistency 문제를 해결할 수 있다.
            NoticeContent.ITEMS.clear();
            NoticeContent.ITEMS.addAll(productItems);


// Set the adapter
            recyclerViewNotice.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    //At this point the layout is complete and the
                    //dimensions of recyclerView and any child views are known.
                }
            });

            if (recyclerViewNotice.getLayoutManager() == null) {
                recyclerViewNotice.setLayoutManager(new LinearLayoutManager(getContext()));
            }


            recyclerViewNotice.setAdapter(myNoticeItemRecyclerViewAdapter);


            mSwipeViewNotice.setRefreshing(false);
//            Log.e("wow", result);

        }

    }









    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        mListener = null;
    }

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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(NoticeItem item);
    }
}
