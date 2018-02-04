package nova.typoapp.groupChat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nova.typoapp.R;
import nova.typoapp.groupChat.ChatTextContent.ChatItem;

import static android.content.Context.ACTIVITY_SERVICE;

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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ///////////소켓 채팅을 위한 변수들


    private static final String ipText = "192.168.242.1"; // tcp 소켓을 연결할, 서버의 ip - 내부 ip. 테스트용
//    private static final String ipText = "115.68.231.13"; // tcp 소켓을 연결할, 서버의 ip
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

    //채팅 리사이클러뷰의 어댑터
    ChatTextRvAdapter chatTextRvAdapter = new ChatTextRvAdapter(ChatTextContent.ITEMS);





    @BindView(R.id.etChatText)
    EditText etChatText;


    Handler msghandler;

    SocketClient client;
    ReceiveThread receive;
    SendThread send;
    Socket socket;

    PipedInputStream sendstream = null;
    PipedOutputStream receivestream = null;

    LinkedList<SocketClient> threadList;


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
        SharedPreferences pref_login = getActivity().getSharedPreferences(getString(R.string.key_pref_Login) , 0);

        userEmail = pref_login.getString("cookie_email","null");
        userName = pref_login.getString("cookie_name","null");

        userProfileUrl = pref_login.getString("cookie_profile_url","");



        //그룹 id 를 액티비티에서 가져온다

        Intent intent = getActivity().getIntent();
        idGroup = intent.getIntExtra("idGroup",-1);

        rvChatList.setLayoutManager(new LinearLayoutManager(getContext()) );


        LinearLayoutManager lm = (LinearLayoutManager)rvChatList.getLayoutManager();

        rvChatList.setAdapter( chatTextRvAdapter );



        threadList = new LinkedList<SocketClient>();


        // ReceiveThread를통해서 받은 메세지를 Handler로 MainThread에서 처리(외부Thread에서는 UI변경이불가)
        msghandler = new Handler() {
            @Override
            public void handleMessage(Message hdmsg) {

                // 메시지 번호가 1111이면 메시지를 수신한 것임.

                if (hdmsg.what == 1111) {

                    try {

                        JSONObject jsonObject = new JSONObject(hdmsg.obj.toString());

                        int idGroup = jsonObject.getInt("idGroup");

                        String chatText = jsonObject.getString("chatText");

                        String chatWriterEmail = jsonObject.getString("chatWriterEmail");

                        String chatWriterName = jsonObject.getString("chatWriterName");

                        String chatTime = jsonObject.getString("chatTime");

                        String chatWriterProfile =  jsonObject.getString("chatWriterProfile");


                        ChatItem chatItem = new ChatItem(idGroup, chatText, chatWriterName , chatWriterEmail, chatWriterProfile,chatTime);

                        //채팅 텍스트를 업데이트 한다.
                        ChatTextContent.ITEMS.add(chatItem);

                        chatTextRvAdapter.notifyDataSetChanged();

                        rvChatList.getLayoutManager().scrollToPosition( chatTextRvAdapter.getItemCount()-1);

//                  tvChatText.append(hdmsg.obj.toString() + "\n");






                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



                }
            }
        };

        return view;
    }

    //채팅 시작 버튼 클릭 이벤트
    @OnClick(R.id.btnStartChat)
    void startChat(){
        //Client 연결부
        client = new SocketClient(ipText, String.valueOf(port) );
        threadList.add(client);
        client.start();

        btnStartChat.setVisibility(View.GONE);

    }

    //채팅 시작 버튼 클릭 이벤트
    @OnClick(R.id.btnSendChat)
    void sendChatText(){

        //SendThread 시작
        if (etChatText.getText().toString() != null) {
            send = new SendThread(socket);
            send.start();

            //시작후 edittext 초기화
            etChatText.setText("");
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Toast.makeText(getContext(), "onDestroy", Toast.LENGTH_SHORT).show();
        ChatTextContent.ITEMS.clear();
        chatTextRvAdapter.notifyDataSetChanged();
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
    ////////////////////// 채팅 관련 클래스들

    class SocketClient extends Thread {
        boolean threadAlive;
        String ip;
        String port;

        //접속 정보로 보낼 맥 -> 앱에서는 이메일로 변경
        String mac;

        //InputStream inputStream = null;
        OutputStream outputStream = null;
        BufferedReader br = null;

        private DataOutputStream output = null;

        public SocketClient(String ip, String port) {
            threadAlive = true;
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {

            try {
                // 연결후 바로 ReceiveThread 시작
                socket = new Socket(ip, Integer.parseInt(port));
                //inputStream = socket.getInputStream();
                output = new DataOutputStream(socket.getOutputStream());
                receive = new ReceiveThread(socket);
                receive.start();


                //사용자 이메일 전송
                output.writeUTF(userEmail);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // 서버로부터 메시지를 받는 스레드.
    // 서버에서 채팅 내용을 받아, 말풍선으로 화면에 보여준다.
    class ReceiveThread extends Thread {
        private Socket socket = null;
        DataInputStream input;

        public ReceiveThread(Socket socket) {
            this.socket = socket;
            try {
                input = new DataInputStream(socket.getInputStream());
            } catch (Exception e) {
            }
        }

        // 메세지 수신후 Handler로 전달
        public void run() {
            try {
                while (input != null) {

                    String msg = input.readUTF();
                    if (msg != null) {
                        Log.d(ACTIVITY_SERVICE, "test");

                        Message hdmsg = msghandler.obtainMessage();
                        hdmsg.what = 1111;
                        hdmsg.obj = msg;
                        msghandler.sendMessage(hdmsg);
//                        Log.d(ACTIVITY_SERVICE, hdmsg.obj.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //서버로 메시지를 보내는 스레드
    //서버로 채팅 내용을 json 형식으로 보낸다.
    //json 에는 채팅 작성자, 내용, 그룹 id(방 구분), 작성 날짜가 들어있다.
    class SendThread extends Thread {
        private Socket socket;
        String msgChatText = etChatText.getText().toString();



        DataOutputStream output;

        public SendThread(Socket socket) {
            this.socket = socket;
            try {
                output = new DataOutputStream(socket.getOutputStream());
            } catch (Exception e) {
            }
        }

        public void run() {

            try {

                // 메세지 전송부 (누군지 식별하기위한 방법으로 mac를 사용)
                Log.d(ACTIVITY_SERVICE, "11111");


                // jsonObject 에 채팅 내용을 세팅한다.
                JSONObject jsonObject = new JSONObject();
                //그룹 id -> 채팅방 구별에 사용
                jsonObject.put("idGroup", idGroup);

                //채팅 내용
                jsonObject.put("chatText", msgChatText );

                //채팅 작성자
                jsonObject.put("chatWriterEmail", userEmail );

                //채팅 작성자
                jsonObject.put("chatWriterName", userName );

                //채팅 프로필 url
                jsonObject.put("chatWriterProfile", userProfileUrl );

                //채팅 내용 작성 시간
                jsonObject.put("chatTime", formattedDate(new Date(), "a h:mm"));

                String jsonText = jsonObject.toString();


                if (output != null) {
                    if (msgChatText != null) {
                        output.writeUTF(jsonText);

                    }
                }
            } catch (IOException | JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }




        // 형식화된 날짜를 얻는 함수.
        //formattedDate(new Date(), "a h:mm"); 를 하면 "오후 11:30" 형식의 데이터를 얻는다.
        public String formattedDate(Date date, String format)
        {
            SimpleDateFormat toFormat = new SimpleDateFormat(format);
            return toFormat.format(date);
        }
    }
























}
