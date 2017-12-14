package nova.typoapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import nova.typoapp.comment.CommentContent;
import nova.typoapp.comment.CommentContent.CommentItem;
import nova.typoapp.comment.MyCommentRecyclerViewAdapter;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.CommentActivity.feedID;
import static nova.typoapp.retrofit.ApiService.API_URL;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class CommentFragment extends Fragment {

// TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;


    private String TAG = "CmtFragTag";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CommentFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static CommentFragment newInstance(int columnCount) {
        CommentFragment fragment = new CommentFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment_list, container, false);

        //먼저 리사이클러뷰에 담을 댓글을 불러온다.
//        CallCommentTask callCommentTask = new CallCommentTask();
//        callCommentTask.execute();


        Toast.makeText(getContext(), "onCreateView Called", Toast.LENGTH_SHORT).show();
//ㅁㄴㅇ
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            recyclerView.setAdapter(new MyCommentRecyclerViewAdapter(CommentContent.ITEMS, mListener));

        }
        return view;
    }






    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            Toast.makeText(context, "onAttach called", Toast.LENGTH_SHORT).show();
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Toast.makeText(getContext(), "on Detach called", Toast.LENGTH_SHORT).show();
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        Toast.makeText(getContext(), "on pause called", Toast.LENGTH_SHORT).show();

    }

    public static void updateCommentList(){


    }

    public class CallCommentTask extends AsyncTask<Void, String, Void> {

        Context context = getActivity();
        ProgressDialog asyncDialog = new ProgressDialog(
                context);


        //온프리에서 다이얼로그를 띄운다.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //시작할 때 댓글 리스트를 클리어하자.
            CommentContent.clearList();

            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("덧글을 불러오는 중입니다...");

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


                    CommentItem productComment = new CommentItem(commentID, feedID, depth, writer, content, writtenDate, profileUrl);
                    CommentContent.addItem(productComment);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }



            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            asyncDialog.dismiss();
        }

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
            void onListFragmentInteraction(CommentItem item);

        }
    }
