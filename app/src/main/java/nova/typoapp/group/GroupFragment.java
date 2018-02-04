package nova.typoapp.group;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import nova.typoapp.group.GroupContent.GroupItem;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.group.GroupContent.ITEMS;
import static nova.typoapp.retrofit.ApiService.API_URL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String TAG = GroupFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public GroupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupFragment newInstance(String param1, String param2) {
        GroupFragment fragment = new GroupFragment();
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




    @BindView(R.id.imgAddGroup)
    ImageView imgAddGroup; //그룹 추가 플러스모양(+) 버튼의 이미지뷰

    @BindView(R.id.rvGroup)
    RecyclerView rvGroup;

    MyGroupRecyclerViewAdapter groupRecyclerViewAdapter = new MyGroupRecyclerViewAdapter(GroupContent.ITEMS);



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        ButterKnife.bind(this, view);

        // + 모양 버튼을 그룹 추가하기 카드뷰에 세팅한다.
        Glide.with(getActivity()).load(R.drawable.add3).into(imgAddGroup);

        // 사용자의 그룹 목록을 받아오는 asyncTask 를 실행한다.
        // 해당 태스크가 끝나면 단어장 목록이 화면에 세팅된다.
        GetGroupTask getGroupTask = new GetGroupTask();
        getGroupTask.execute();


        //멤버 회원이면 가입버튼을 보이지 않게 한다.






        return view;
    }


    // 새 그룹 추가하기를 클릭했을 때 발생하는 이벤트.
    @OnClick(R.id.cvAddGroup)
    public void clickAddSet() {

        Intent intent = new Intent(getContext(), AddGroupActivity.class);
        startActivity(intent);

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


    public class GetGroupTask extends AsyncTask<Void, String, String> {

        String json_result;
        private Context mContext = getContext();

        List<GroupItem> productItems = new ArrayList<>();

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

            Call<ResponseBody> comment = apiService.getGroupList();

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

                    int idGroup = jObject.getInt("id_group");

                    String nameGroup = jObject.getString("name_group");

                    String contentGroup = jObject.getString("content_group");

                    String emailGroupOwner = jObject.getString("email_group_owner");

                    String nameGroupOwner = jObject.getString("name_group_owner");

                    int numGroupMembers = jObject.getInt("num_group_members");

                    String dateGroupMade = jObject.getString("date_group_made");



                    String profileUrl = "";
                    if (!jObject.getString("img_url_group").equals("")) {
                        profileUrl = jObject.getString("img_url_group");
                    }


                    boolean isMemberGroup =  Boolean.parseBoolean( jObject.getString("isMemberGroup") );



                    //그룹 아이템 객체 생성자를 통해 아이템에 그룹 데이터를 담는다.
                    Log.e(TAG, "doInBackground: " + nameGroup + numGroupMembers + nameGroupOwner);
                    GroupItem productGroupItem = new GroupItem(idGroup, nameGroup, contentGroup,emailGroupOwner, nameGroupOwner, profileUrl, numGroupMembers, dateGroupMade, isMemberGroup);

                    //그룹 아이템을 그룹 아이템 리스트에 추가한다.
                    productItems.add(productGroupItem);

//                    for(int j = 0; j<ITEMS.size(); j++){
//
//                        Log.e(TAG, "onPostExecute: "+productItems.get(i).nameGroupOwner);
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

                GroupItem item = ITEMS.get(j);
                Log.e(TAG, "onPostExecute: " + item.nameGroupOwner + item.numGroupMembers + item.nameGroup);

            }

            // 리사이클러뷰 어댑터를 갱신한다.
            rvGroup.setLayoutManager(new LinearLayoutManager(getContext()));

            rvGroup.setAdapter(groupRecyclerViewAdapter);
            groupRecyclerViewAdapter.notifyDataSetChanged();


        }

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
