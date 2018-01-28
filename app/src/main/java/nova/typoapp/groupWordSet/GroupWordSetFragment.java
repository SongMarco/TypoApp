package nova.typoapp.groupWordSet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
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
import nova.typoapp.wordset.WordSetContent;
import nova.typoapp.wordset.WordSetContent.WordSetItem;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;
import static nova.typoapp.wordset.WordSetContent.ITEMS;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupWordSetFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupWordSetFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupWordSetFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public GroupWordSetFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupWordSetFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupWordSetFragment newInstance(String param1, String param2) {
        GroupWordSetFragment fragment = new GroupWordSetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @BindView(R.id.imgAddGroupWs)
    ImageView imgAddGroupWs;

    @BindView(R.id.rvGroupWordSet)
    RecyclerView rvGroupWordSet;



    MyGroupWordSetAdapter groupWordSetAdapter = new MyGroupWordSetAdapter(WordSetContent.ITEMS);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_word_set, container, false);

        ButterKnife.bind(this, view);


        Glide.with(getActivity()).load(R.drawable.add3).into(imgAddGroupWs);



        //그룹의 단어장을 가져온다.
        GetGroupWsTask getGroupWsTask = new GetGroupWsTask();
        getGroupWsTask.execute();


        return view;
    }

    //단어장 공유하기를 눌렀다.
    @OnClick(R.id.cvAddGroupWs)
    void addGroupWs() {


        //유저의 단어장 리스트를 가져와 다이얼로그에 세팅해야한다.

        //어댑터에 단어장 이름을 세팅하는 어싱크 태스크를 실행한다.
        GetWordSetListTaskInDialog getWordSetListTaskInDialog = new GetWordSetListTaskInDialog(getActivity());
        getWordSetListTaskInDialog.execute();


    }


    public class GetWordSetListTaskInDialog extends AsyncTask<Void, String, String> {

        String json_result;

        private Context mContext;

        ArrayAdapter<String> arrayAdapter;

        List<WordSetItem> itemSet = new ArrayList<>();

        int selectedItem;


        public GetWordSetListTaskInDialog(Context context) {

            mContext = context;
            this.arrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.select_dialog_singlechoice);

        }

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


                    //아이템 객체에 데이터를 다 담은 후, 아이템을 어레이어댑터에 추가한다.


//                    Log.e(TAG, "doInBackground: " + nameSet + numSetWords + nameSetOwner);
                    WordSetItem productWordSetItem = new WordSetItem(idWordSet, nameSet, numSetWords, nameSetOwner, profileUrl);

                    itemSet.add(productWordSetItem);

                    arrayAdapter.add(productWordSetItem.nameWordSet);
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


            AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext)

                    .setTitle("단어장을 선택하세요")
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Toast.makeText(mContext, arrayAdapter.getItem(selectedItem) + "단어장 선택", Toast.LENGTH_SHORT).show();

                            //이름을 기준으로 단어장을 추가하고 있음. 추후 단어장 생성시 중복 체크 필요

                            //단어장을 그룹에 추가하는 어싱크 태스크 수행

                            String nameWordSet = arrayAdapter.getItem(selectedItem);

                            AddGroupWsTask addGroupWsTask = new AddGroupWsTask(nameWordSet);
                            addGroupWsTask.execute();



                            dialog.dismiss();
                        }
                    });


            builderSingle.setAdapter(arrayAdapter, null);

            AlertDialog dialog = builderSingle.create();

            dialog.getListView().setOnItemClickListener(
                    new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
// do your stuff here

                            //parent 내부 뷰의 체크 상태를 해제한다.
                            for (int i = 0; i < parent.getChildCount(); i++) {
                                //체크를 확인할 뷰
                                CheckedTextView checkedView = (CheckedTextView) parent.getChildAt(i);

                                //뷰가 체크된 상태라면
                                if(checkedView.isChecked()){

                                    checkedView.setChecked(false);
                                }

                            }

                            CheckedTextView checkedTextView = (CheckedTextView) view;
                            ((CheckedTextView) view).setChecked(true);

                            selectedItem = position;
                        }
                    });


            dialog.show();
//            //받아온 리스트를 다이얼로그의 어레이 어댑터에 세팅한다.


        }

    }




    //서버에서 그룹에 단어장을 추가하도록 하는 태스크
    public class AddGroupWsTask extends AsyncTask<Integer, String, String> {

        String json_result;
        private Context mContext = getContext();

        String nameWordSet;

        public AddGroupWsTask(String nameWordSet){

            this.nameWordSet = nameWordSet;
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


            // 레트로핏 콜 객체를 만든다. 파라미터로 게시물의 ID값, 게시물의 타입을 전송한다.


            //GroupActivity 에서 그룹 id 값을 가져온다.
            int idGroup = getActivity().getIntent().getIntExtra("idGroup", -1);

            Call<ResponseBody> comment = apiService.addGroupWordSet(nameWordSet, idGroup );

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

            GetGroupWsTask getGroupWsTask = new GetGroupWsTask();
            getGroupWsTask.execute();


        }

    }

    public void updateWordSet() {
//        GetWordSetListTask getWordSetListTask = new GetWordSetListTask();
//        getWordSetListTask.execute();
    }



    public class GetGroupWsTask extends AsyncTask<Void, String, String> {

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

            int idGroup = getActivity().getIntent().getIntExtra("idGroup", -1);

            Call<ResponseBody> comment = apiService.getGroupWordSet(idGroup);

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

                    String nameSet = jObject.getString("name_set");

                    String emailSetOwner = jObject.getString("email_set_owner");

                    String nameSetOwner = jObject.getString("name_set_owner");

                    int numSetLike = jObject.getInt("num_set_like");

                    int numSetWords = jObject.getInt("num_set_words");

//                    int numSetTaken = jObject.getInt("num_set_taken");


                    String dateSetMade = jObject.getString("date_set_made");


                    String profileUrl = "";
                    if (!jObject.getString("owner_img_profile").equals("")) {
                        profileUrl = jObject.getString("owner_img_profile");
                    }


                    //아이템 객체에 데이터를 다 담은 후, 아이템을 리스트에 추가한다.

                    WordSetItem productWordSetItem = new WordSetItem(idWordSet, nameSet, numSetWords, nameSetOwner, profileUrl);

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


            rvGroupWordSet.setLayoutManager(new LinearLayoutManager(getContext()));

            rvGroupWordSet.setAdapter(groupWordSetAdapter);
            groupWordSetAdapter.notifyDataSetChanged();


        }

    }


















    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
