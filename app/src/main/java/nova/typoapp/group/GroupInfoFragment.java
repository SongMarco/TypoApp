package nova.typoapp.group;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import nova.typoapp.groupMember.GroupMemberContent.MemberItem;
import nova.typoapp.groupMember.MyGroupMemberAdapter;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.groupMember.GroupMemberContent.ITEMS;
import static nova.typoapp.retrofit.ApiService.API_URL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public GroupInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupInfoFragment newInstance(String param1, String param2) {
        GroupInfoFragment fragment = new GroupInfoFragment();
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

    int idGroup;
    String nameGroup;
    String contentGroup;
    String UrlGroupImg;
    int numGroupMembers;
    boolean isMemberGroup;


    @BindView(R.id.groupName)
    TextView tvGroupName;

    @BindView(R.id.groupContent)
    TextView tvGroupContent;

    @BindView(R.id.tvListMember)
    TextView tvListMember;

    @BindView(R.id.imgGroupInfo)
    ImageView imgGroupInfo;

    @BindView(R.id.buttonApplyGroup)
    Button btnApplyGroup;

    @BindView(R.id.rvGroupMember)
    RecyclerView rvGroupMember;



    MyGroupMemberAdapter groupMemberAdapter = new MyGroupMemberAdapter(ITEMS);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_info, container, false);
        ButterKnife.bind(this,view);


        // 그룹 정보 프래그먼트 초기화에 필요한 변수를 그룹 액티비티에서 가져온다.

        Intent intent = getActivity().getIntent();

        idGroup = intent.getIntExtra("idGroup",-1);


        nameGroup = intent.getStringExtra("nameGroup");
        contentGroup = intent.getStringExtra("contentGroup");
        UrlGroupImg = intent.getStringExtra("UrlGroupImg");
        numGroupMembers = intent.getIntExtra("numGroupMembers",-1);

        //가입 여부를 확인하는 변수
        isMemberGroup = intent.getBooleanExtra("isMemberGroup",false);






        // 그룹 정보 뷰를 세팅한다.

        tvGroupName.setText(nameGroup); //그룹명 세팅
        tvGroupContent.setText(contentGroup); //그룹 설명 세팅
        tvListMember.setText("회원  "+numGroupMembers); //회원 수 세팅

        Glide.with(getActivity()).load(UrlGroupImg).into(imgGroupInfo); // 그룹 이미지 세팅

        // 사용자가 멤버로 되어 있다면, isMemberGroup 값이 true이고, 멤버가 아니라면 false 이다.

        //사용자가 그룹의 멤버인 경우
        if(isMemberGroup){

            btnApplyGroup.setVisibility(View.GONE);

        }
        else{

            btnApplyGroup.setVisibility(View.VISIBLE);
        }




        //그룹 멤버 리스트를 가져오는 어싱크태스크 수
        new GetGroupMembersTask().execute();









        return view;



    }


    @OnClick(R.id.buttonApplyGroup)
    void applyGroup(){

        new ApplyGroupTask().execute();

    }



    public class ApplyGroupTask extends AsyncTask<Void, String, String> {

        String json_result;
        private Context mContext = getActivity();


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



            /////@@@@@ 레트로핏 콜 객체 생성
            Call<ResponseBody> retrofitCall = apiService.applyGroup(idGroup);

            try {
                json_result = retrofitCall.execute().body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }



        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            //가입이 적용되었다. 멤버 리스트를 갱신한다.
            new GetGroupMembersTask().execute();

            //가입을 했으므로, 가입 버튼을 보이지 않게 한다.
            btnApplyGroup.setVisibility(View.INVISIBLE);



        }

    }











    public class GetGroupMembersTask extends AsyncTask<Void, String, String> {

        String json_result;
        private Context mContext = getActivity();

        List<MemberItem> productItems = new ArrayList<>();

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

            Call<ResponseBody> comment = apiService.getGroupMembers(idGroup);

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

                    String nameMember = jObject.getString("name_member");

                    String emailMember = jObject.getString("email_member");

                    String profileUrl = "";
                    if (!jObject.getString("profile_url").equals("")) {
                        profileUrl = jObject.getString("profile_url");
                    }

                    //아이템 객체에 데이터를 다 담은 후, 아이템을 리스트에 추가한다.

                    MemberItem productItem = new MemberItem(idGroup, nameMember, emailMember, profileUrl);

                    productItems.add(productItem);


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


            //리사이클러뷰의 어댑터를 세팅한다.
            rvGroupMember.setLayoutManager(new LinearLayoutManager(getContext()));

            rvGroupMember.setAdapter(groupMemberAdapter);
            groupMemberAdapter.notifyDataSetChanged();


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
