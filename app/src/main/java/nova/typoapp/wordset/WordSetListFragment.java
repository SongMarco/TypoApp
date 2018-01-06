package nova.typoapp.wordset;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

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
import nova.typoapp.wordset.WordSetContent.WordSetItem;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;
import static nova.typoapp.wordset.WordSetContent.ITEMS;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */

/*
WordSetListFragment

1. 클래스 개요

 - 단어장 리스트 페이지를 보여주기 위한 프래그먼트

 - 단어장을 추가하거나 단어장 목록을 조회한다.

2. 클래스 구성

 1) AddWordSetTask : 단어장을 추가하기 위한 비동기 태스크
 2) GetWordSetTask : 서버에서 단어장 리스트를 불러오는 비동기 태스크
 3) 그 외 프래그먼트 기본 컴포넌트 등

3. 클래스 흐름 설명

 1) 단어장 추가하기

    a. 상단의 단어장 추가를 클릭하면 단어장 이름을 입력받는 다이얼로그를 띄운다.
    b. 다이얼로그의 editText 에 단어장 이름을 입력받는다.
    c. AddWordSetTask 가 실행되어, 서버의 단어장 DB에 해당 이름의 단어장을 추가한다.
    d. 위의 비동기 태스크가 끝나면 GetWordSetTask 가 실행되어, 서버의 단어장 목록을 가져와 화면을 업데이트한다.

 2) 단어장 목록 초기화 / 새로고침
    a. 프래그먼트가 초기화될 때(onCreateView) 에서 GetWordSetTask 가 실행된다.
    b. 위의 비동기 태스크가 실행되면 단어장 목록을 업데이트하여 화면에 보여준다.
    c. 이 흐름은 단어장을 추가할 때에도 동작한다.

 3) 단어장 조회하기
    a. 단어장 리스트의 아이템을 클릭하면 단어장 액티비티(WordSetActivity) 로 넘어간다.
    b. 단어장 액티비티에서 내부 컨텐츠를 이용할 수 있다.(학습, 퍼즐 등)

4. 특이사항
 - 단어장 리사이클러뷰의 어댑터가 세팅되지 않았다는 경고 로그가 확인되나, 실제로는 정상적으로 작동되고 있다.

 */




public class WordSetListFragment extends Fragment {


    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String TAG = "WordSetListFragment";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WordSetListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static WordSetListFragment newInstance(int columnCount) {
        WordSetListFragment fragment = new WordSetListFragment();
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


    @BindView(R.id.rvWordSet)
    RecyclerView rvWordSet;

    @BindView(R.id.cvAddWordSet)
    CardView cvAddWordSet;

    @BindView(R.id.imgAddWordSet)
    ImageView imgAddWordSet;



    String nameWordSet;

    MyWordSetItemRecyclerViewAdapter rvWordSetAdapter = new MyWordSetItemRecyclerViewAdapter(ITEMS, mListener);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wordsetitem_list, container, false);
        ButterKnife.bind(this,view);


        Glide.with(getActivity()).load(R.drawable.add3).into(imgAddWordSet);

        rvWordSet.setNestedScrollingEnabled(false);



//        // Set the adapter
//        if (view instanceof RecyclerView) {
//            Context context = view.getContext();
//            RecyclerView recyclerView = (RecyclerView) view;
//
//            recyclerView.setLayoutManager(new LinearLayoutManager(context));
//
//            recyclerView.setAdapter(rvWordSetAdapter);
//        }


        GetWordSetListTask getWordSetListTask = new GetWordSetListTask();
        getWordSetListTask.execute();


        return view;
    }


    public void updateRecyclerView(){

        rvWordSetAdapter.notifyDataSetChanged();

    }


    @OnClick(R.id.cvAddWordSet)
    public void clickAddSet(){

//        Toast.makeText(getActivity(), "세트 추가 클릭", Toast.LENGTH_SHORT).show();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("새 단어장 추가");       // 제목 설정
//        builder.setMessage("");   // 내용 설정



// EditText 삽입하기
        final EditText etNameWordSet = new EditText(getActivity());

        etNameWordSet.setHint(R.string.hint_etNameWordSet);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(60, 0, 60, 0);


        layout.addView(etNameWordSet, params);


//        et.setLayoutParams();
        builder.setView(layout);

// 확인 버튼 설정
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Log.v(TAG, "Yes Btn Click");

                // Text 값 받아서 로그 남기기


                // 에딧텍스트에 제목 입력을 했다면 서버로 입력 정보를 보내고, 단어장을 세팅해준다.
                if(etNameWordSet.getText()!=null &&!etNameWordSet.getText().toString().equals("")){
                    nameWordSet = etNameWordSet.getText().toString();
                    Log.v(TAG, "nameWordSet = "+nameWordSet);



                    AddWordSetTask addWordSetTask = new AddWordSetTask();
                    addWordSetTask.execute();



                }
                else{
                    Toast.makeText(getActivity(), "에러 : 단어장 이름을 입력하지 않으셨네요!", Toast.LENGTH_SHORT).show();
                }

                nameWordSet = etNameWordSet.getText().toString();
                Log.v(TAG, "nameWordSet = "+nameWordSet);

                dialog.dismiss();     //닫기
                // Event
            }
        });

// 중립 버튼 설정
        builder.setNeutralButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Log.v(TAG,"Neutral Btn Click");
                dialog.dismiss();     //닫기
                // Event
            }
        });

//// 취소 버튼 설정
//        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
////                Log.v(TAG,"No Btn Click");
//                dialog.dismiss();     //닫기
//                // Event
//            }
//        });

// 창 띄우기
        builder.show();


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
        void onListFragmentInteraction(WordSetItem item);
    }

    public class GetWordSetListTask extends AsyncTask<Void, String, String> {

        String json_result;
        private Context mContext = getContext();

        List<WordSetItem> productItems = new ArrayList<>();

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

            Call<ResponseBody> comment = apiService.getWordSetList();

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

                    //jsonArray 의 데이터를 댓글 아이템 객체에 담는다.
                    JSONObject jObject = jsonRes.getJSONObject(i);  // JSONObject 추출

                    int idWordSet = jObject.getInt("id_wordset");

                    String nameSet = jObject.getString("set_name");

                    String emailSetOwner = jObject.getString("email_set_owner");

                    String nameSetOwner = jObject.getString("name_set_owner");

                    int numSetWords = jObject.getInt("num_set_words");

                    int numSetTaken = jObject.getInt("num_set_taken");

                    int numSetLike = jObject.getInt("num_set_like");


                    String dateSetMade = jObject.getString("date_set_made");


                    String profileUrl = "";
                    if (!jObject.getString("profile_url").equals("")) {
                        profileUrl = jObject.getString("profile_url");
                    }


                    //아이템 객체에 데이터를 다 담은 후, 아이템을 리스트에 추가한다.


                    Log.e(TAG, "doInBackground: " + nameSet + numSetWords + nameSetOwner);
                    WordSetContent.WordSetItem productWordSetItem = new WordSetContent.WordSetItem(idWordSet, nameSet, numSetWords, nameSetOwner, profileUrl);

                    productItems.add(productWordSetItem);

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


            for (int j = 0; j < ITEMS.size(); j++) {

                WordSetItem item = ITEMS.get(j);
                Log.e(TAG, "onPostExecute: " + item.nameWordSetOwner + item.numWords + item.nameWordSet);

            }

            rvWordSet.setLayoutManager(new LinearLayoutManager(getContext()));

            rvWordSet.setAdapter(rvWordSetAdapter);
            rvWordSetAdapter.notifyDataSetChanged();


        }

    }



    //서버에서 단어장을 추가하도록 하는 태스크
    public class AddWordSetTask extends AsyncTask<Integer, String, String> {

        String json_result;
        private Context mContext = getContext();



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


            // 레트로핏 콜 객체를 만든다. 파라미터로 게시물의 ID값, 게시물의 타입을 전송한다.

            Call<ResponseBody> comment = apiService.addWordSet(nameWordSet);

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

            //서버로 데이터 전송이 완료되었다.
            //단어장 데이터를 갱신한다.

            GetWordSetListTask getWordSetListTask = new GetWordSetListTask();
            getWordSetListTask.execute();


        }

    }

    public void updateWordSet(){
        GetWordSetListTask getWordSetListTask = new GetWordSetListTask();
        getWordSetListTask.execute();
    }


}
