package nova.typoapp.groupMember;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.R;
import nova.typoapp.group.GroupActivity;
import nova.typoapp.groupChat.GroupChatService;
import nova.typoapp.groupMember.GroupMemberContent.MemberItem;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.group.GroupActivity.isDeleteMemberMode;
import static nova.typoapp.retrofit.ApiService.API_URL;

/**
 * Created by Administrator on 2018-01-29.
 */

public class MyGroupMemberAdapter extends RecyclerView.Adapter<MyGroupMemberAdapter.ViewHolder> {

    private final List<MemberItem> mValues;

    public MyGroupMemberAdapter(List<MemberItem> items) {
        mValues = items;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public MyGroupMemberAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())

                //새로 어댑터 생성할 경우 아래 레이아웃 교체하기
                .inflate(R.layout.fragment_group_info_member_item, parent, false);
        return new MyGroupMemberAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyGroupMemberAdapter.ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        final MemberItem item = holder.mItem;

        //멤버 이름을 세팅한다.
        holder.tvNameMember.setText(item.nameMember);

        // 프로필 이미지의 유무에 따라 프로필 이미지뷰 세팅. 없으면 -> 기본 세팅


        //멤버 이미지 뷰를 세팅한다.
        if (item.imgUrlMEmber != null && !item.imgUrlMEmber.equals("")) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(holder.mView).load(item.imgUrlMEmber)
                    .apply(requestOptions)
                    .into(holder.imgProfileMember);
        }


        //멤버의 등급에 따라 텍스트뷰를 다르게 표시
        switch (item.levelMember) {

            //일반 회원인 경우
            case 1:

                holder.tvLevelMember.setText("회원");

                break;


            //운영진인 경우
            case 2:
                holder.tvLevelMember.setText("운영진");


                break;

            //그룹장인 경우
            case 3:

                holder.tvLevelMember.setText("그룹장");


                break;


        }

        //멤버 관리 모드이고, 멤버 레벨이 2 이하일 경우 강퇴 가능
        if (isDeleteMemberMode && item.levelMember < 3) {

            //강퇴 버튼을 세팅
            holder.imgBanMember.setVisibility(View.VISIBLE);


        }
        //강퇴 버튼을 안 보이게 함
        else {
            holder.imgBanMember.setVisibility(View.GONE);
        }


        final int viewId = holder.mView.getId();


        //클릭 이벤트 주석 처리
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int idClicked = view.getId();


                //멤버 삭제 버튼을 클릭했다. 삭제 확인 다이얼로그를 띄우고, 삭제 승인시 삭제 태스크 진행
                if (idClicked == holder.imgBanMember.getId()) {

//                Toast.makeText(view.getContext(), "아이템 클릭 : "+item.nameMember, Toast.LENGTH_SHORT).show();

                    final Context mContext = view.getContext();

                    AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
                    alert.setTitle("회원 삭제");
                    alert.setMessage("정말 삭제하시겠습니까?");

                    //확인 버튼
                    alert.setPositiveButton("예", new DialogInterface.OnClickListener() {


                        //삭제를 확인했다. 회원 삭제 태스크를 진행한다.
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            GroupActivity groupActivity = (GroupActivity) mContext;

                            //그룹 id 를 그룹 액티비티에서 가져온다.
                            int idGroup = groupActivity.getIntent().getIntExtra("idGroup", -1 );


                            new DeleteMemberTask(idGroup, item, mContext ).execute();

                            dialog.dismiss();
                        }
                    });

                    alert.setNegativeButton("아니오", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                        }
                    });

                    alert.show();

                }


            }


        };

        //아이템에 클릭 리스너 세팅
//        holder.mView.setOnClickListener(clickListener);
        holder.imgBanMember.setOnClickListener(clickListener);


    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public MemberItem mItem;

        @BindView(R.id.imgProfileMember)
        ImageView imgProfileMember;

        @BindView(R.id.tvNameMember)
        TextView tvNameMember;

        @BindView(R.id.tvLevelMember)
        TextView tvLevelMember;

        @BindView(R.id.imgBanMember)
        ImageView imgBanMember;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;


        }

    }


    //그룹을 탈퇴하는 태스크 - 회원 스스로 탈퇴할 때, 그룹장이 회원을 강퇴시킬 때 사용
    public class DeleteMemberTask extends AsyncTask<Void, Void, Void> {

        int idGroup;
        String memberEmail;
        String memberName;

        Context mContext ;

        //방나감 처리를 위해 서비스와 연결 필요
        GroupChatService chatService;

        String json_result;

        //파라미터로 그룹 id, 멤버 이메일을 받는다.


        public DeleteMemberTask(int idGroup, MemberItem memberItem, Context context) {
            this.idGroup = idGroup;
            this.memberEmail = memberItem.emailMember;
            this.memberName = memberItem.nameMember;

            this.mContext = context;
        }



        @Override
        protected Void doInBackground(Void... voids) {


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
            Call<ResponseBody> retrofitCall = apiService.leaveGroup(idGroup, memberEmail);

            try {
                //레트로핏 콜 수행 -> http 통신 수행. 회원 탈퇴 진행
                json_result = retrofitCall.execute().body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }





            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);

            //멤버 리사이클러뷰를 갱신

            GroupActivity groupActivity = (GroupActivity)mContext;

//            Toast.makeText(groupActivity,, Toast.LENGTH_SHORT).show();

            //뷰페이저 이동을 통해 프래그먼트 갱신
            groupActivity.notifyFragmentChanged();

            //강퇴를 시켰는데 회원이 채팅중이었을 경우에는 -> tcp 서버로 해당 회원의 exitChat 메시지를 보내 채팅방에서 내보내야 한다.

            //일단 if 문 없이 진행
//            sendExitChatForBannedUser(idGroup, memberEmail, memberName);






        }

        //강퇴된 유저가 채팅방을 나가게 하는 메소드.
        public void sendExitChatForBannedUser(int idGroup, String memberEmail, String memberName){

            // jsonObject 에 채팅 종료 메세지를 세팅한다.
            String jsonExitMsg = makeJsonExitMsg("exitChat", idGroup, memberEmail, memberName);

            GroupActivity groupActivity = (GroupActivity)mContext;

            //서비스에 바인드하여 메세지를 서버로 보낸다.

            //채팅 서비스를 바인드한다.

            //채팅 서비스를 위한 인텐트 생성
            Intent intent = new Intent(
                   groupActivity, // 현재 화면
                    GroupChatService.class); // 다음넘어갈 컴퍼넌트


            //바인딩 순서 : bindService -> 서비스에서 onBind 호출 -> 서비스커넥션에서 onServiceConnected 호출 -> Service.startChat 으로 채팅 시작
            groupActivity.bindService(intent, // intent 객체
                    new ConnToService(jsonExitMsg), // 서비스와 연결에 대한 정의
                    Context.BIND_AUTO_CREATE);







        }


        //서비스에 연결하기 위한 커넥션
        class ConnToService implements ServiceConnection {

            String jsonExitMsg;

            public ConnToService(String jsonExitMsg){
                this.jsonExitMsg = jsonExitMsg;
            }

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                // 서비스와 연결되었을 때 호출되는 메서드
                // 서비스 객체를 전역변수로 저장
                GroupChatService.ChatBinder chatBinder = (GroupChatService.ChatBinder) iBinder;
                chatService = chatBinder.getService(); // 서비스가 제공하는 메소드 호출하여

                chatService.sendExitChat(jsonExitMsg);

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        }

    }






    public String makeJsonExitMsg(String msgType, int idGroup, String userEmail, String userName) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("idGroup", idGroup);
            //보낼 json 메시지의 타입 -> 채팅방 접속인지, 나간 건지, 채팅인지 구별할 때 사용
            //현재 채팅 종료 메시지를 보낼 것이므로, exitChat 으로 타입을 세팅
//            String msgType = "exitChat";
            jsonObject.put("msgType", msgType);

            //채팅 내용 : 메시지 타입이 채팅일 때만 사용됨
            jsonObject.put("chatText", userName + " 님이 그룹 채팅에서 나갔습니다.");

            //채팅 접속한 사람 이메일
            jsonObject.put("userEmail", userEmail);

            //채팅 접속자 이름
            jsonObject.put("userName", userName );

            //채팅 접속자 프로필 사진 url
            jsonObject.put("userProfileUrl", "");

            //채팅 접속 시간
            jsonObject.put("chatTime", "");

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return jsonObject.toString();
    }

}

