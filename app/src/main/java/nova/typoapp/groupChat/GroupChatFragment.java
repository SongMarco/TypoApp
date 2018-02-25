package nova.typoapp.groupChat;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nova.typoapp.R;
import nova.typoapp.groupChat.ChatTextContent.ChatItem;
import nova.typoapp.groupChat.groupChatSqlite.MySQLiteOpenHelper;
import nova.typoapp.groupChat.ottoEventBus.BusProvider;
import nova.typoapp.groupChat.ottoEventBus.ChatRcvEvent;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GroupChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GroupChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


//그룹 채팅을 위한 프래그먼트


public class GroupChatFragment extends Fragment {


    GroupChatService chatService; // 그룹 채팅 서비스의 객체


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ///////////소켓 채팅을 위한 변수들

    //현재 채팅 중인 그룹의 id 값.
    //수신한 채팅 메시지의 그룹 id 값과 해당 id 값이 일치할 경우, 채팅이 채팅 알림시 사용된다.
    public static int idGroupStatic = -1;


    private static final String ipText = "192.168.242.1"; // tcp 소켓을 연결할, 서버의 ip - 내부 ip. 테스트용
    //        private static final String ipText = "115.68.231.13"; // tcp 소켓을 연결할, 서버의 ip
    private static int port = 9999; // 채팅 서버의 포트


//    private static int port = 9999; // 채팅 서버의 포트


    //채팅 시작 버튼
    @BindView(R.id.btnStartChat)
    Button btnStartChat;

    @BindView(R.id.btnSendChat)
    Button btnSendChat;

    //채팅 리사이클러뷰
    @BindView(R.id.rvChatList)
    RecyclerView rvChatList;

    @BindView(R.id.layoutChat)
    LinearLayout layoutChat;


    //채팅 리사이클러뷰의 어댑터
    ChatTextRvAdapter chatTextRvAdapter = new ChatTextRvAdapter(ChatTextContent.ITEMS);


    @BindView(R.id.etChatText)
    EditText etChatText;




    //채팅 시작을 위한 커넥션
    ConnStartChat connStartChat;

    //채팅 방 id
    int idGroup;



    /////////////////////////////////

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public GroupChatFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GroupChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GroupChatFragment newInstance(String param1, String param2) {
        GroupChatFragment fragment = new GroupChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Otto bus provider 를 프래그먼트에 등록한다. ( 채팅 서비스(GroupChatService) 로부터 발생하는 이벤트를 감지하기 위함 )
        BusProvider.getInstance().register(this);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    //사용자 정보 - 쉐어드 프리퍼런스에 저장돼있음
    String userEmail; // 사용자 이메일
    String userName; // 사용자 이름

    String userProfileUrl; //사용자 프로필 이미지


    @SuppressLint("HandlerLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);

        // 버터나이프를 프래그먼트에 바인드 시킨다

        ButterKnife.bind(this, view);


        //셰어드에서 로그인토큰을 가져온다.
        SharedPreferences pref_login = getActivity().getSharedPreferences(getString(R.string.key_pref_Login), 0);

        userEmail = pref_login.getString("cookie_email", "null");
        userName = pref_login.getString("cookie_name", "null");

        userProfileUrl = pref_login.getString("cookie_profile_url", "");


        //그룹 id 를 액티비티에서 가져온다

        Intent intent = getActivity().getIntent();
        idGroup = intent.getIntExtra("idGroup", -1);
        idGroupStatic = idGroup;



        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()));


        LinearLayoutManager lm = (LinearLayoutManager) rvChatList.getLayoutManager();

        rvChatList.setAdapter(chatTextRvAdapter);


        return view;
    }

    //채팅 메시지를 수신했을 때 채팅 화면을 업데이트하는 메소드
    @Subscribe
    public void rcvChat(final ChatRcvEvent chatRcvEvent) {

        //리사이클러뷰를 갱신한다. -> ui 변경이 필요하므로 runOnUiThread 사용
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e("jsonHere", "handleMessage: " + chatRcvEvent.toString());


                //채팅 메시지의 채팅방 ID 값이 현재 채팅방 ID 값과 동일하다면
                //-> 채팅 아이템 리스트에 추가하고, 채팅 ui를 갱신한다.
                if(chatRcvEvent.chatItem.idGroup == idGroup){
                    //채팅 아이템 리스트에 추가
                    ChatTextContent.ITEMS.add(chatRcvEvent.chatItem);

                    //채팅 리사이클러뷰 어댑터 notify -> ui 갱신
                    chatTextRvAdapter.notifyDataSetChanged();

                    //채팅 스크롤을 맨 밑으로 이동
                    rvChatList.getLayoutManager().scrollToPosition(chatTextRvAdapter.getItemCount() - 1);

                }


            }
        });

    }

    public class ConnStartChat implements ServiceConnection{

        public ConnStartChat(){

        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            GroupChatService.ChatBinder chatBinder = (GroupChatService.ChatBinder) iBinder;
            chatService = chatBinder.getService(); // 서비스가 제공하는 메소드 호출하여


            //채팅을 시작하는 asyncTask 수행 (네트워크를 사용하므로, 메인 스레드에서 startChat 메소드 호출 불가능)
            new startChatTask().execute();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }





    //채팅 시작 버튼 클릭시 발생하는 이벤트
    @OnClick(R.id.btnStartChat)
    void startChat() {
        // 채팅방에서 채팅을 시작한다.

        // 채팅 서비스를 채팅 프래그먼트와 바인드하여, 서비스에서 소켓 통신 수행 후 채팅 프래그먼트의 채팅뷰(리사이클러뷰)를 업데이트 할 수 있게 한다.

        //채팅 소켓 클라이언트를 초기화 하고, 채팅을 입력할 수 있도록 채팅 관련 뷰들을 세팅한다.

        //채팅 서비스를 위한 인텐트 생성
        Intent intent = new Intent(
                getActivity(), // 현재 화면
                GroupChatService.class); // 다음넘어갈 컴퍼넌트

        connStartChat = new ConnStartChat();

        //채팅 서비스를 바인드한다.
        //바인딩 순서 : bindService -> 서비스에서 onBind 호출 -> 서비스커넥션에서 onServiceConnected 호출 -> Service.startChat 으로 채팅 시작
        getActivity().bindService(intent, // intent 객체
                connStartChat, // 서비스와 연결에 대한 정의
                Context.BIND_AUTO_CREATE);


        //채팅 시작 버튼을 보이지 않도록 하고, 채팅 입력 레이아웃을 보이도록 하여 채팅을 진행
        btnStartChat.setVisibility(View.GONE);
        layoutChat.setVisibility(View.VISIBLE);
    }


    //채팅 시작 버튼 클릭 이벤트
    @OnClick(R.id.btnSendChat)
    void sendChatText() {


        // 채팅 입력란이 비지 않은 상태에서 보내기를 누르면
        if (!etChatText.getText().toString().equals("")) {

            //채팅 서비스의 sendChat 메소드를 호출, 채팅 메세지를 채팅 서버로 보내게 한다.
            chatService.sendChat(idGroup, userEmail, userName, userProfileUrl, etChatText.getText().toString());

            //에딧 텍스트를 초기화한다.
            etChatText.setText("");

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


    //채팅 프래그먼트에서 나오게 됨.
    @Override
    public void onDestroy() {
        super.onDestroy();

//        Toast.makeText(getContext(), "onDestroy", Toast.LENGTH_SHORT).show();
        ChatTextContent.ITEMS.clear();
        chatTextRvAdapter.notifyDataSetChanged();

        // 이벤트 버스 등록 해제 (채팅 서비스 이벤트 버스)
        BusProvider.getInstance().unregister(this);

//        try {
//
//
//
//            socket.close();
//
//
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


    }


    // 화면을 전환할 경우 -> 로컬 db 에는 이미 채팅 내역이 저장돼 있으므로, 따로 저장 조치를 안해도 된다.
    // 채팅 내역 불러오기를 onResume 에서 수행하면 채팅을 이어서 할 수 있다.
    // @소켓 관리는?? 따로 소켓 제거 안해도 ok. 메세지 계속 수신 -> 저장함. 불러오기시 모두 복원됨
    @Override
    public void onPause() {
        super.onPause();

        //채팅 프래그먼트에서 화면을 전환할 경우 -> 알림을 받아야 함 -> idGroupStatic -1로.
        idGroupStatic = -1;
        //onResume 에서는 채팅 화면이 복구됨 -> 알림 필요 없음 -> idGroupStatic 원래 값으로 복구

    }


    // onResume
    // 다른 액티비티로 넘어갔다가, 채팅을 하러 다시 왔을 때
    // 채팅방에서 저장한 채팅 내역을 불러와 리사이클러뷰에 세팅한다.
    @Override
    public void onResume() {
        super.onResume();

        //onResume 에서는 채팅 화면이 복구됨 -> 알림 필요 없음 -> idGroupStatic 원래 값으로 복구
        idGroupStatic = idGroup;


        String dbName = "dbChat.db"; //db 이름. 브라우저에서 조회시 해당 파일명으로 DB 조회 가능(Stetho 등)
        int dbVersion = 2; // 데이터베이스 버전 : 앱이 바뀌면서 데이터베이스를 업그레이드할 때 필요.(DB 버전 관리)

        SQLiteDatabase dbChat; // db 객체

        //로컬 DB(SQLite) 의 초기화
        // 채팅 db를 가져오는데 필요한 SQLite OpenHelper 객체를 초기화
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(
                getActivity(),  // 현재 화면의 제어권자
                dbName,// db 이름
                null,  // 커서팩토리-null : 표준커서가 사용됨
                dbVersion);       // 버전

        //openHelper 를 이용, 채팅 내용 db를 가져온다.
        dbChat = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB


        //채팅방의 채팅 내역을 db 에서 가져온다. idGroup 을 기준으로 채팅방을 구분하였다.
        Cursor c = dbChat.rawQuery("select * from chatTable where idGroup = " + idGroup + ";", null);

        //불러온 채팅 내역으로부터 채팅 아이템을 추가할 아이템 리스트를 만든다.
        List<ChatItem> cloneItems = new ArrayList<>();

        while (c.moveToNext()) {

            //채팅 아이템으로부터 db에 저장할 변수들을 가져온다.
            int idGroup = c.getInt(1); //그룹(채팅방) id
            int chatType = c.getInt(2); //채팅 메세지 타입 : 일반 채팅 / 공지사항(누가 들어오거나 나갔을 때) 구별
            String chatText = c.getString(3); // 채팅 메세지 내용
            String chatWriterName = c.getString(4); // 메세지 발신자 이름
            String chatWriterEmail =c.getString(5); // 메세지 발신자 이메일
            String chatWriterProfile = c.getString(6);// 메세지 발신자 프로필 url
            String chatTime = c.getString(7); // 메세지 발송 시간

            ChatItem loadChatItem = new ChatItem(idGroup, chatType, chatText,chatWriterName, chatWriterEmail, chatWriterProfile, chatTime);

            cloneItems.add(loadChatItem);
        }
        c.close();

//        채팅 내역이 불러와짐. -> 채팅 방에 참여중 -> 채팅 내용 세팅 후 소켓 연결. 채팅모드로 사용
        if (cloneItems.size() != 0) {

            //리사이클러뷰에 사용할 채팅 리스트에 불러온 채팅 내역을 복사
            ChatTextContent.ITEMS.addAll(cloneItems);

            //채팅 리사이클러뷰 어댑터 notify 후, 채팅 맨 밑부분으로 스크롤 이동
            chatTextRvAdapter.notifyDataSetChanged();
            rvChatList.getLayoutManager().scrollToPosition(chatTextRvAdapter.getItemCount() - 1);


            //채팅 서비스(소켓 통신)을 위한 인텐트 생성
            Intent intent = new Intent(
                    getActivity(), // 현재 화면
                    GroupChatService.class); // 다음넘어갈 컴퍼넌트

            connStartChat = new ConnStartChat();

            //채팅 서비스를 바인드한다.
            //바인딩 순서 : bindService -> 서비스에서 onBind 호출 -> 서비스커넥션에서 onServiceConnected 호출 -> Service.startChat 으로 채팅 시작
            getActivity().bindService(intent, // intent 객체
                    connStartChat, // 서비스와 연결에 대한 정의 - 채팅 시작(지금 상황은 재개, resume)
                    Context.BIND_AUTO_CREATE);


            //채팅 시작 버튼 안 보이게, 채팅 입력창 보이게 한다.
            btnStartChat.setVisibility(View.GONE);
            layoutChat.setVisibility(View.VISIBLE);

        }
        //채팅 내역 없음 -> 채팅 방 참여 인원이 아님 -> 채팅 시작 버튼을 세팅
        else {

            //채팅 시작 버튼을 보이게 하고, 채팅 입력 레이아웃을 안 보이게 한다. -> 추후 생명주기, 소켓 상태에 따라 변경
            btnStartChat.setVisibility(View.VISIBLE);

            layoutChat.setVisibility(View.GONE);
        }


    } // end of onResume

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
    ////////////////////// 채팅 관련 클래스들


//    String noticeText = userName+" 님이 그룹 채팅에 참여했습니다.";
//    ChatItem noticeItem = new ChatItem(idGroup, CHAT_NOTICE, noticeText,  userName, userEmail);
//                ChatTextContent.ITEMS.add(noticeItem);
//
//                chatTextRvAdapter.notifyDataSetChanged();


    //현재 채팅방을 나가는 메소드
    public void exitChat() {
//        Toast.makeText(getActivity(), "채팅을 종료합니다", Toast.LENGTH_SHORT).show();


        chatService.exitChat();
        getActivity().unbindService(connStartChat);




        ChatTextContent.ITEMS.clear();
        chatTextRvAdapter.notifyDataSetChanged();

        btnStartChat.setVisibility(View.VISIBLE);

        layoutChat.setVisibility(View.GONE);



    }


    //채팅을 시작하는 asyncTask.
    public class startChatTask extends AsyncTask<Integer, String, String> {


        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.


        @Override
        protected String doInBackground(Integer... integers) {

            chatService.startChat(idGroup, userEmail, userName, userProfileUrl);

            return null;
        }

        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);


        }

    }


}
