package nova.typoapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

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
import nova.typoapp.retrofit.ApiService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class NewsFeedFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewsFeedFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static NewsFeedFragment newInstance(int columnCount) {
        NewsFeedFragment fragment = new NewsFeedFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }


    public void updateRecyclerViewNewsFeed(){

        recyclerViewNewsFeed.setLayoutManager(new LinearLayoutManager(getContext() )  );
        recyclerViewNewsFeed.setAdapter(myNewsFeedRecyclerViewAdapter);
        myNewsFeedRecyclerViewAdapter.notifyDataSetChanged();

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

    @BindView(R.id.layoutAdd)
    CardView layoutAdd;


    String json_result;



    MyNewsFeedRecyclerViewAdapter myNewsFeedRecyclerViewAdapter = new MyNewsFeedRecyclerViewAdapter(NewsFeedContent.ITEMS, new MyNewsFeedRecyclerViewAdapter.ClickListener() {
        @Override
        public void onPositionClicked(int position) {

        }

        @Override
        public void onLongClicked(int position) {

        }
    }

    );


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_newsfeed_list, container, false);

        ButterKnife.bind(this, view);

        Toast.makeText(getActivity(), "onCreateViewCalled", Toast.LENGTH_SHORT).show();
        layoutAdd.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        Toast.makeText(getContext(), "글을씁시다", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), WriteActivity.class);
                        startActivity(intent);
                }


                return false;
            }



        });
        //DB에서 회원 정보를 확인하고 로그인하라. 단, 두번은 안하게!

        //call이 되었다면 해당 어싱크를 수행하지 않는다.
//        if(! NewsFeedContent.called ){
//            NewsFeedContent.taskCallFeeds taskCallFeeds = new NewsFeedContent.taskCallFeeds();
//            taskCallFeeds.execute();
//
//        }

        //region 게시물 리스트 불러오기. 주의사항 - 불러오기 이후 어댑터를 세팅해주어야, 제때 뷰를 반환해준다.
        // 게시물 리스트 불러오기
        Retrofit retrofit = new Retrofit.Builder().baseUrl(ApiService.API_URL).build();
        ApiService apiService = retrofit.create(ApiService.class);


        NewsFeedContent.ITEMS.clear();

        Call<ResponseBody> comment = apiService.getFeedList();


        final ProgressDialog asyncDialog = new ProgressDialog(
                getActivity());
        asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        asyncDialog.setMessage("글을 불러오는 중입니다...");
        // show dialog
        asyncDialog.show();

        comment.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
//                                       Log.v("Test", response.body().string());

                    json_result = response.body().string();
//                    Log.v("RECV DATA", json_result);

                    JSONArray jsonRes = null;
                    try {
                        jsonRes = new JSONArray(json_result);

                        for (int i = 0; i < jsonRes.length(); i++) {
                            JSONObject jObject = jsonRes.getJSONObject(i);  // JSONObject 추출

                            int feedNum = jObject.getInt("feedNum");
                            String writer = jObject.getString("writer");
                            String title = jObject.getString("title");
                            String content = jObject.getString("text_content");
                            String writtenDate = jObject.getString("written_time");
                            String imgUrl = "";
                            String profileUrl = "";
                            if(!Objects.equals(jObject.getString("imgUrl"), "")){
                                imgUrl = jObject.getString("imgUrl");
                            }
                            if(! jObject.getString("writer_profile").equals("") ){

                                profileUrl = jObject.getString("writer_profile");
                            }


//                            Log.v("hey", writer+title+content);

//                            FeedItem productFeed = NewsFeedContent.createFeed4(writer, title, content, imgUrl);
                            FeedItem productFeed = NewsFeedContent.createFeed7(feedNum, writer, title, content, imgUrl, profileUrl, writtenDate);
                            NewsFeedContent.addItem(productFeed);


                        }

// Set the adapter
                        if (recyclerViewNewsFeed != null) {
                            recyclerViewNewsFeed.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    asyncDialog.dismiss();
                                    //At this point the layout is complete and the
                                    //dimensions of recyclerView and any child views are known.
                                }
                            });
                            Context context = view.getContext();

                            if (mColumnCount <= 1) {
                                recyclerViewNewsFeed.setLayoutManager(new LinearLayoutManager(context));
                            } else {
                                recyclerViewNewsFeed.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                            }
                            recyclerViewNewsFeed.setAdapter(myNewsFeedRecyclerViewAdapter);
                            recyclerViewNewsFeed.setNestedScrollingEnabled(false);
                        }




//                        for (int i = 0; i < NewsFeedContent.ITEMS.size(); i++) {
//
////                            Log.e("wow", (i + " : " + NewsFeedContent.ITEMS.get(i).getClass() ) );
////                            Log.e("wow", (i + " : " + NewsFeedContent.ITEMS.get(i).getInfo() ) );
//
//                        }
//



                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
        //endregion





        return view;
    }

//
//    @OnClick(R.id.layoutAdd)
//    void onAddClick() {
////        Toast.makeText(getContext(), "글을씁시다", Toast.LENGTH_SHORT).show();
//
//        Intent intent = new Intent(getContext(), WriteActivity.class);
//        startActivity(intent);
//
//
//    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Toast.makeText(getActivity(), "onAttachCalled", Toast.LENGTH_SHORT).show();
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
    public void onResume() {
        super.onResume();
        ButterKnife.bind(this,getActivity());
        Toast.makeText(getActivity(), "onResumeFragCalled", Toast.LENGTH_SHORT).show();


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
        void onListFragmentInteraction(FeedItem item);
    }


}
