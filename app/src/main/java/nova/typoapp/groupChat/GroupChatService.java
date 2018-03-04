package nova.typoapp.groupChat;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import nova.typoapp.MainActivity;
import nova.typoapp.R;
import nova.typoapp.group.GroupActivity;
import nova.typoapp.group.GroupContent.GroupItem;
import nova.typoapp.groupChat.ChatTextContent.ChatItem;
import nova.typoapp.groupChat.groupChatSqlite.MySQLiteOpenHelper;
import nova.typoapp.groupChat.ottoEventBus.BusProvider;
import nova.typoapp.groupChat.ottoEventBus.ChatRcvEvent;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.groupChat.ChatTextContent.CHAT_NOTICE;
import static nova.typoapp.groupChat.GroupChatFragment.idGroupStatic;
import static nova.typoapp.retrofit.ApiService.API_URL;


/*
* GroupChatService
* 그룹 채팅에서 클라이언트가 채팅 서버와 메세지를 주고 받기 위한 서비스.
*
* 통신은 TCP 소켓 기반으로, 서버와 json 형태의 메세지를 주고 받는다.
* 백그라운드에서 동작하므로, 앱을 종료해도 서버와 통신이 가능하다.
*
* 서버로부터 채팅 메세지를 받아 채팅 내역 DB에 저장하고,
* 현재 채팅방을 열지 않은 클라이언트에게 알림 메세지를 보낸다.(알림은 fcm 기반)
* (@@유저가 알림 메세지 수신을 거부할 수 있어야 함)
*
* case 1) 채팅방을 꺼둔 상태 : 채팅 서비스에서 메세지 수신, 로컬 DB 저장
* case 2) 채팅방을 켜둔 상태일 경우 : 채팅 서비스에서 메세지 수신, 로컬 DB 저장 -> 채팅 프래그먼트의 리사이클러뷰에 채팅 메세지를 추가하여 갱신
*
*
*
* @@ 채팅 서비스의 흐름
*
* 채팅 프래그먼트에서 채팅 시작 버튼을 누른다 -> 채팅 서비스 바인드 후 채팅 시작(소켓이 없을 경우 소켓을 생성)
*
* 채팅 진행 : 서비스에서 계속 소켓 통신, 채팅 프래그먼트 ui(채팅 리사이클러뷰) 업데이트 진행(otto 이벤트 버스 사용)
*
* 채팅방 나감 : 서비스에서 해당 방에 대한 소켓 통신 종료
*
* 채팅 부재(화면 전환, onStop) : 서비스에서 계속 소켓 통신.
*
*
*
* 특이사항 / 의문사항
*
* 채팅 프래그먼트와 어떻게 채팅 데이터를 주고 받지? otto 이벤트 버스를 사용하였다.
* ex) 서버에서 채팅 메시지를 수신하는 이벤트 발생(post)
* -> 현재 채팅방의 메시지일 경우, 채팅 프래그먼트에서 이벤트 버스를 받아서(subscribe) 채팅 ui 업데이트, 수신한 채팅 메시지를 보여주게 됨
*
*/


public class GroupChatService extends Service {

    //아래 변수들은 서버로 메세지를 클라이언트에서 보낼 때 사용된다. -> 채팅 중에만 사용되는 변수.
    int idGroup; //현재 참여한 채팅방의 id
    String userName; // 클라이언트 유저 이름
    String userEmail; // 클라이언트 유저 메일
    String userProfileUrl; //클라이언트 프로필 사진 url

    Socket socket; //채팅 소켓
    DataOutputStream outPutStream; // 채팅 소켓의 아웃풋 스트림
    DataInputStream inputStream; // 채팅 소켓의 인풋 스트림


    ReceiveThread receive; //채팅 메세지 수신 스레드

    IBinder mBinder = new ChatBinder(); //채팅 프래그먼트와의 바인딩에 사용되는 바인더

//    private static final String ipText = "192.168.242.1"; // tcp 소켓을 연결할, 서버의 ip - 내부 ip. 테스트용
        private static final String ipText = "115.68.231.13"; // tcp 소켓을 연결할, 서버의 ip
    private static int port = 9999; // 채팅 서버의 포트


    public void setIdGroup(int idGroup) {
        this.idGroup = idGroup;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;


        // 그룹챗 프래그먼트에서 서비스 바인딩이 일어난다.
        return mBinder;
    }


    /*서비스가 언바인드될 때
    * case 1 : 채팅방 프래그먼트에서 다른 화면으로 갔을 때
    *
    * */
    @Override
    public boolean onUnbind(Intent intent) {

        // 채팅방 id를 초기화
        this.idGroup = -1;

        return super.onUnbind(intent);

    }

    //바인더 클래스
    public class ChatBinder extends Binder {
        //바인더에서 서비스를 가져오는 메소드
        public GroupChatService getService() {
            return GroupChatService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.d("test", "서비스의 onCreate");

        //서비스가 만들어질 때, 소켓을 초기화하여 서버와 연결한다.
        //메인 스레드에서 소켓을 만들 수 없으므로, asyncTask 로 소켓 초기화(InitSocketTask 클래스에 ctrl+b를 눌러 참조)
        new InitSocketTask().execute();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.d("test", "서비스의 onStartCommand");

//        return super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행 -> 서비스는 어지간하면 죽지 않는데, 안드로이드에서 앱을 죽일 때(kill) 죽게됨

        //
//        // 소켓을 닫고, 채팅 메시지 수신 스레드를 null 로 만든다.
//        try {
//            socket.close();
//            socket = null;
//            Log.e("ondestr", "onDestroy: socket closed" );
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Log.e("ondestr", "onDestroy: socket closed" );

    }


    //작업관리자에서 앱을 종료할 때
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.e("restart", "onTaskRemoved:started " );

        //앱을 재시작하기 위한 서비스 인텐트 만듬
        Intent intentRestartService = new Intent(getApplicationContext(),this.getClass());

        intentRestartService.setPackage(getPackageName());


        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,intentRestartService, PendingIntent.FLAG_ONE_SHOT);


        //알람매니저로 서비스 재시작.
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 100,
                restartPendingIntent);


        Log.e("restart", "onTaskRemoved:finished " );
        super.onTaskRemoved(rootIntent);


    }

    //채팅 서비스에서 유저 정보(방 id, 이름, 이메일 등)를 이용하여, 채팅을 시작하는 메소드
    public void startChat(int idGroup, String userEmail, String userName, String userProfileUrl) {


        //현재 채팅방에 참여한 유저의 정보를 초기화한다. -> 채팅 시작, 채팅 resume 모두 적용됨.
        //이후 채팅 메시지를 보낼 경우 -> 아래의 정보로 채팅 메시지를 서버로 전송한다.
        this.idGroup = idGroup;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userProfileUrl = userProfileUrl;


        //해당 유저가 채팅을 시작했음을 서버에 알린다.
        sendJoinChat();


    }

    //서비스에서 유저 정보를 이용해, 채팅 메시지를 서버로 보내주는 메소드
    public void sendChat(int idGroup, String userEmail, String userName, String userProfileUrl, String chatText) {

        //채팅 메세지를 보내는데 필요한 변수를 파라미터로부터 가져와 세팅
        this.idGroup = idGroup;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userProfileUrl = userProfileUrl;


        //채팅 메세지를 보내는 스레드를 구동한다.
        SendThread send = new SendThread(chatText);
        send.start();

    }

    //채팅을 종료했음을 알리는 메소드. 회원 강퇴시 사용
    public void sendExitChat(String jsonExitChat){

        if (!socket.isClosed()) {
            //소켓에서 아웃풋 스트림을 가져오고, 스트림을 통해 json 채팅 나감 메시지를 서버로 전달
            try {
                outPutStream.writeUTF(jsonExitChat);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }



    //소켓이 생성될 때 서버에 유저 정보를 보내는 메소드
    //서비스를 시작할 때 서버에 있는 채팅방에서 유저의 소켓을 갱신하기 위해 사용한다.

    //서버에서 방에 있는 유저들에게 메세지를 돌릴 때 -> 유저이메일-소켓 해쉬맵을 참조해서 메세지를 보내면 된다.
    public void sendUserInfo() {
        try {

            //채팅 방에 접속한 유저들에게 사용자가 채팅방에 접속했다는 메세지 전송

            //보낼 json 메시지의 타입 -> 채팅방 접속인지, 나간 건지, 채팅인지 구별할 때 사용
            String msgType = "userInfo";


            //sharedPref 에서 유저의 이메일 정보를 가져온다.

            //셰어드에서 로그인토큰을 가져온다.
            SharedPreferences pref_login = getSharedPreferences(getString(R.string.key_pref_Login), 0);
            userEmail = pref_login.getString("cookie_email", "");

            String jsonString = makeJsonInfo(msgType, userEmail);

            if (!socket.isClosed()) {
                //소켓에서 아웃풋 스트림을 가져오고, 스트림을 통해 json 채팅 나감 메시지를 서버로 전달
                outPutStream.writeUTF(jsonString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    ////////////////채팅 관련 파트

    //서버에 유저가 접속했음을 알리는 메소드
    public void sendJoinChat() {
        //사용자가 방에 처음 접속을 한 것이라면 -> 사용자가 채팅방에 접속했다는 메세지 전송

        //채팅 텍스트의 리스트 크기가 0이면 -> 저장된 채팅 내용이 없으므로 처음 채팅을 한 것이다.
        if (ChatTextContent.ITEMS.size() == 0) {

            try {

                //채팅 방에 접속한 유저들에게 사용자가 채팅방에 접속했다는 메세지 전송

                //보낼 json 메시지의 타입 -> 채팅방 접속인지, 나간 건지, 채팅인지 구별할 때 사용
                String msgType = "joinChat";

                String jsonString = makeJsonMsg(msgType, userName + " 님이 그룹 채팅에 참가했습니다.");


                //소켓에서 아웃풋스트림 생성
//                DataOutputStream outPutStream = new DataOutputStream(socket.getOutputStream());

                if (!socket.isClosed()) {
                    //소켓에서 아웃풋 스트림을 가져오고, 스트림을 통해 json 채팅 나감 메시지를 서버로 전달
                    outPutStream.writeUTF(jsonString);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //채팅 텍스트의 리스트 크기가 0이 아니면 -> 저장된 채팅 내용이 있음 -> 채팅을 이어서 하는 것이다.
        else {


            try {


                // 서버에 사용자가 채팅을 재개했다는 메세지를 전송

                //보낼 json 메시지의 타입 -> 채팅방 접속인지, 나간 건지, 채팅인지 구별할 때 사용
                String msgType = "resumeChat";

//                // jsonObject 에 채팅 내용을 세팅한다.
//                JSONObject jsonObject = createJsonObject(idGroup, msgType, "", userEmail, userName, userProfileUrl);
//                //그룹 id -> 채팅방 구별에 사용

                String jsonString = makeJsonMsg(msgType, userName + " 님이 그룹 채팅을 재개합니다.");


                outPutStream.writeUTF(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    // 서버로부터 메시지를 받는 스레드.
    // 서버에서 채팅 내용을 받아, 말풍선으로 화면에 보여준다.
    class ReceiveThread extends Thread {


        String msg;

        public ReceiveThread(Socket socket) {

        }

        //채팅 메시지 수신 스레드 시작
        public void run() {

            while (inputStream != null) {

                //먼저, 채팅에 필요한 json 메시지를 읽어들여, 채팅 아이템 객체로 만들고, 이 채팅 아이템을 리스트에 추가한다.
                try {
                    //인풋 스트림에서 서버가 보내온 json 메세지를 읽어들인다.
                    msg = inputStream.readUTF();


                    JSONObject jsonObject = new JSONObject(msg.toString());

                    int idGroup = jsonObject.getInt("idGroup");

                    String chatText = jsonObject.getString("chatText");

                    String userEmail = jsonObject.getString("userEmail");

                    String userName = jsonObject.getString("userName");

                    String chatTime = jsonObject.getString("chatTime");

                    String userProfileUrl = jsonObject.getString("userProfileUrl");


                    String msgType = jsonObject.getString("msgType");

                    ChatItem chatItem;
                    //수신한 메세지가 일반 채팅일 경우
                    if (msgType.equals("sayChat")) {

                        //채팅 아이템을 일반 채팅 내용 아이템으로 설정
                        chatItem = new ChatItem(idGroup, chatText, userName, userEmail, userProfileUrl, chatTime);
                    }
                    //수신한 메세지가 공지사항일 경우
                    else {

                        //채팅 아이템을 공지 채팅 아이템으로 설정
                        chatItem = new ChatItem(idGroup, CHAT_NOTICE, chatText, userName, userEmail);

                    }


                    //채팅 내용을 채팅 내용 DB에 추가하여 저장한다. -> 이후 채팅 내역 불러오기에 사용된다.
                    saveChat(chatItem);


                    //알림 허용 / 거부를 체크한다.

                    //알림 거부한 방 목록이 필요하다.
                    SharedPreferences pref_notification = getSharedPreferences(getString(R.string.key_pref_notification), Activity.MODE_PRIVATE);



                    //쉐어드 프리퍼런스에서 알림 거부된 방 id를 가져와본다.(거부하지 않았다면 -1 이 나옴)
                    int refusedIdGroup = pref_notification.getInt(String.valueOf(idGroup), -1);



                    //채팅방 화면의 그룹 id 값 != 수신한 메시지의 id값 인 경우 && 알림을 허용했다면 -> 알림 전달 필요.
                    if (idGroupStatic != chatItem.idGroup && idGroup != refusedIdGroup) {


                        //그룹 아이템을 http 통신으로 가져온다. -> groupItemForNotice 가 세팅된다.
                        GetGroupWithIdTask getGroupWithIdTask = new GetGroupWithIdTask(chatItem);
                        getGroupWithIdTask.execute();

                        //위의 태스크가 끝나면, 클라이언트에 알림을 전달한다.


                    }
                    //채팅방 화면의 그룹 id 값 == 수신한 메시지의 id값 인 경우 -> 알림 전달 필요없고, ui 업데이트 필요함.
                    else {
                        //@@ 채팅 ui 업데이트
                        //채팅 프래그먼트의 그룹 id와, 위에서 만든 채팅 아이템의 그룹 id 가 같으면 -> 현재 채팅 중임. ui 업데이트

                        // 채팅 프래그먼트의 UI(채팅 리사이클러뷰)를 갱신하기 위해 이벤트 버스를 발행(post)한다.
                        // -> 채팅방 프래그먼트가 켜진 상태이고, 채팅 메시지가 담길 채팅방이라면, 받아온 채팅 내용이 보이게 된다.

                        // 또한 프래그먼트가 활성화되지 않은 경우 프래그먼트로 이벤트 전달이 되지 않으므로, ui 업데이트가 되지 않는다.
                        BusProvider.getInstance().post(new ChatRcvEvent(chatItem));
                    }


                } catch (IOException | JSONException e) {


                    e.printStackTrace();
                    // 소켓이 끊김 -> 무한루프 중단
                    break;
                }


            }
        }
    }


    // 클라이언트에서 채팅 메세지 알림을 만드는 메소드
    public void makeNotification(ChatItem chatItem, GroupItem groupItemForNotice) {

        //알림이 전달되면 들어갈 화면 ( 그룹 액티비티 )

        //todo 그룹 액티비티 -> 채팅 프래그먼트 호출 필요.
        //호출에 대한 변수를 인텐트에 전달하여, 겟 아이템을 시키면 될 듯하다.
        Intent intentGroup = new Intent(this, GroupActivity.class);

        //채팅 메세지 알림을 위한 그룹 아이템. 아래의 GetGroupWithIdTask 에서 사용됨


        //chatItem 정보를 바탕으로 그룹 아이템 정보를 http 로 가져온다.


        //알림으로 들어간 화면에서 전달할 값 : 그룹 액티비티 초기화를 위한 변수들 (그룹 id, 그룹명 등) 전달
        intentGroup.putExtra("idGroup", groupItemForNotice.idGroup);

        intentGroup.putExtra("nameGroup", groupItemForNotice.nameGroup);

        intentGroup.putExtra("contentGroup", groupItemForNotice.contentGroup);
        intentGroup.putExtra("UrlGroupImg", groupItemForNotice.UrlGroupImg);

        intentGroup.putExtra("numGroupMembers", groupItemForNotice.numGroupMembers);

        intentGroup.putExtra("isMemberGroup", groupItemForNotice.isMemberGroup);


        //그룹 액티비티 뷰페이저를 채팅 프래그먼트로 이동시키는 변수
        intentGroup.putExtra("goChatFragment", true);


        //알림 화면 뒤의 메인 화면
        Intent intentMain = new Intent(this, MainActivity.class);

        //메인 액티비티 뷰페이저를 그룹 프래그먼트로 이동시키는 변수
        intentMain.putExtra("goGroupFragment", true);


        //@@@ TaskStackBuilder 를 만들고, 위의 인텐트를 추가한다 -> 백스택에 추가됨.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        //@@@ 액티비티 스택 순서 : 메인->알림. 알림에서 뒤로가기 할 경우 -> 메인으로 감

        //메인 액티비티 추가
        stackBuilder.addNextIntentWithParentStack(intentMain);

        //알림 액티비티 추가
        stackBuilder.addNextIntentWithParentStack(intentGroup);

        //@@@ 액티비티 스택을 포함한 PendingIntent 를 생성한다.
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //알림 빌더를 통해 알림을 만든다.
        //안드로이드 O 버전부터는 알림 빌더에서 알림 채널도 세팅할 것을 요구하고 있음. 아래 방식은 deprecated 임에 유의
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle(chatItem.chatWriterName) // 타이틀 : 채팅 보낸 사람의 이름 세팅
                .setContentText(chatItem.chatText) // 서브 타이틀 : 보낸 사람의 채팅 내용 세팅
                .setTicker(chatItem.chatText) // 티커 부분 - 굳이 필요 없음. 화면 상단에 노출되는 알림 메세지인데, 5.0 이상 버전부터는 보이지 않게 되어 있음
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(Notification.CATEGORY_MESSAGE)

                    //notification 의 priority 를 설정한다.
                    //priority 가 HIGH 이상일 경우, 현재 화면의 상단에 바로 알림을 세팅한다. - 이를 헤드업 알림이라 한다.
                    //DEFAULT 이하일 경우, 푸시 알림 메세지 목록에 보이게 된다.
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        //알림을 클라이언트에 세팅한다.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1234, builder.build());


    }

    // http 통신으로 그룹 아이템 정보를 가져오는 태스크
    class GetGroupWithIdTask extends AsyncTask<Void, Void, Void> {

        Context mContext = GroupChatService.this;
        String json_result;

        int idGroup;
        ChatItem chatItem;

        GroupItem productGroupItem;


        public GetGroupWithIdTask(ChatItem chatItem) {
            this.chatItem = chatItem;
            this.idGroup = chatItem.idGroup;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(Void... voids) {

            //그룹 id 정보를 바탕으로, 그룹 item 정보를 가져온다.

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

            Call<ResponseBody> comment = apiService.getGroupWithId(idGroup);

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

//                    int idGroup = jObject.getInt("id_group");

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


                    boolean isMemberGroup = Boolean.parseBoolean(jObject.getString("isMemberGroup"));


                    //그룹 아이템 객체 생성자를 통해 아이템에 그룹 데이터를 담는다.
                    productGroupItem = new GroupItem(idGroup, nameGroup, contentGroup, emailGroupOwner, nameGroupOwner, profileUrl, numGroupMembers, dateGroupMade, isMemberGroup);


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void avoid) {
            super.onPostExecute(avoid);


            //클라이언트에 알림을 전달한다.
            makeNotification(chatItem, productGroupItem);


        }
    }


    //서버로 메시지를 보내는 스레드
    //서버로 채팅 내용을 json 형식으로 보낸다.
    //json 에는 채팅 작성자, 내용, 그룹 id(방 구분), 작성 날짜가 들어있다.
    class SendThread extends Thread {

        String chatText;


        public SendThread(String chatText) {

            this.chatText = chatText;

//            try {
//                output = new DataOutputStream(socket.getOutputStream());
//            } catch (Exception e) {
//            }
        }

        //서버로 메세지를 전송한다.
        public void run() {

            try {
                //사용자가 보낼 채팅 메세지를 json 으로 만든다.
                //메세지 타입은 'sayChat' 인데, 사용자가 입력한 채팅 내용을 의미한다.
                String jsonText = makeJsonMsg("sayChat", chatText);

                //서버에 json 채팅 메세지를 전송한다.
                if (outPutStream != null) {
                    if (chatText != null) {

                        outPutStream.writeUTF(jsonText);

                    }
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }


    }

    //채팅 내용을 채팅 내역 db에 저장하는 메소드
    public void saveChat(ChatItem chatItem) {

        String dbName = "dbChat.db"; //db 이름. 브라우저에서 조회시 해당 파일명으로 DB 조회 가능(Stetho 등 라이브러리를 사용하면 간편함)
        int dbVersion = 2; // 데이터베이스 버전 : 앱이 바뀌면서 데이터베이스를 업그레이드할 때 필요.(DB 버전 관리)

        SQLiteDatabase db; // db 객체

        //로컬 DB(SQLite) 의 초기화
        // 채팅 db를 가져오는데 필요한 SQLite OpenHelper 객체를 초기화
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(
                this,  // 현재 화면의 제어권자
                dbName,// db 이름
                null,  // 커서팩토리-null : 표준커서가 사용됨
                dbVersion);       // 버전

        try {
            //openHelper 를 이용, 채팅 내용 db를 가져온다.
            db = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB


            //채팅 아이템으로부터 db에 저장할 변수들을 가져온다.
            int idGroup = chatItem.idGroup; //그룹(채팅방) id
            int chatType = chatItem.chatItemType; //채팅 메세지 타입 : 일반 채팅 / 공지사항(누가 들어오거나 나갔을 때) 구별
            String chatText = chatItem.chatText; // 채팅 메세지 내용
            String chatWriterName = chatItem.chatWriterName; // 메세지 발신자 이름
            String chatWriterEmail = chatItem.chatWriterEmail; // 메세지 발신자 이메일
            String chatWriterProfile = chatItem.chatWriterProfile; // 메세지 발신자 프로필 url
            String chatTime = chatItem.chatTime; // 메세지 발송 시간


            //채팅 내용 db에, 위에서 가져온 채팅 아이템 변수들을 저장한다. -> db 내용을 불러오면 채팅 아이템을 재생성 가능, 이후 채팅 내역 불러오기에 사용됨
            db.execSQL("insert into chatTable (idGroup, chatType, chatText, userName, userEmail, userProfileUrl, chatTime) " +
                    "values(" + idGroup + ", " + chatType + ", '" + chatText + "', '" + chatWriterName + "', '" + chatWriterEmail + "', '" + chatWriterProfile + "' ,'" + chatTime + "' );");

//            아래는 채팅 db 확인용 코드. 저장 테스트시 사용해볼 것
//            Cursor c = db.rawQuery("select * from chatTable;", null);
//            while (c.moveToNext()) {
//                int id = c.getInt(0);
//                String profileUrl = c.getString(6);
//                Log.d("testDb", "id:" + id + ",proflie:" + profileUrl);
//            }

        }
        //db 가져오기에서 오류가 날 경우
        catch (SQLiteException e) {
            //에러를 로그로 표시함
            e.printStackTrace();
//            Log.e(tag, "데이터베이스를 얻어올 수 없음");
        }
        //로컬 db 초기화 끝


    }

    //패러미터 방 id를 가진 채팅방의 채팅 내역을 제거하는 메소드
    public void removeChatHistory(int idGroup) {


        String dbName = "dbChat.db"; //db 이름. 브라우저에서 조회시 해당 파일명으로 DB 조회 가능(Stetho 등)
        int dbVersion = 2; // 데이터베이스 버전 : 앱이 바뀌면서 데이터베이스를 업그레이드할 때 필요.(DB 버전 관리)

        SQLiteDatabase db; // db 객체

        //로컬 DB(SQLite) 의 초기화
        // 채팅 db를 가져오는데 필요한 SQLite OpenHelper 객체를 초기화
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(
                this,  // 현재 화면의 제어권자
                dbName,// db 이름
                null,  // 커서팩토리-null : 표준커서가 사용됨
                dbVersion);       // 버전

        try {
            //openHelper 를 이용, 채팅 내용 db를 가져온다.
            db = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB


            //채팅 내용 db에, 위에서 가져온 채팅 아이템 변수들을 저장한다. -> db 내용을 불러오면 채팅 아이템을 재생성 가능, 이후 채팅 내역 불러오기에 사용됨
            db.execSQL("delete from chatTable where idGroup = " + idGroup + "; ");

//            아래는 채팅 db 확인용 코드. 저장 테스트시 사용해볼 것
            Cursor c = db.rawQuery("select * from chatTable where idGroup = " + idGroup + ";", null);
            while (c.moveToNext()) {
                int id = c.getInt(0);
                String profileUrl = c.getString(6);
                Log.d("testDb", "id:" + id + ",proflie:" + profileUrl);
            }

        }
        //db 가져오기에서 오류가 날 경우
        catch (SQLiteException e) {
            //에러를 로그로 표시함
            e.printStackTrace();
//            Log.e(tag, "데이터베이스를 얻어올 수 없음");
        }
        //로컬 db 초기화 끝
    }


    //서버에 보내는데 필요한 json 메세지를 만드는 메소드

    //메세지의 유형과, 채팅 내용을 파라미터로 받는다.
    //메세지 유형이 sayChat(일반 채팅 내용) 이 아닐 경우(채팅 나감, 채팅 비움 등) -> 파라미터를 ""로 넣는다.
    // id, 유저 이메일, 이름, 프로필은 모든 메세지에 적용되므로 따로 파라미터로 받지 않음

    public String makeJsonMsg(String msgType, String chatText) {

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("idGroup", idGroup);
            //보낼 json 메시지의 타입 -> 채팅방 접속인지, 나간 건지, 채팅인지 구별할 때 사용
            //현재 채팅 종료 메시지를 보낼 것이므로, exitChat 으로 타입을 세팅
//            String msgType = "exitChat";
            jsonObject.put("msgType", msgType);

            //채팅 내용 : 메시지 타입이 채팅일 때만 사용됨
            jsonObject.put("chatText", chatText);

            //채팅 접속한 사람 이메일
            jsonObject.put("userEmail", userEmail);

            //채팅 접속자 이름
            jsonObject.put("userName", userName);

            //채팅 접속자 프로필 사진 url
            jsonObject.put("userProfileUrl", userProfileUrl);

            //채팅 접속 시간
            jsonObject.put("chatTime", formattedDate(new Date(), "a h:mm"));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return jsonObject.toString();
    }


    //유저이메일-소켓 해쉬맵에 유저 소켓을 저장하기 위해 유저 이메일을 보내는 메소드
    // 유저 이메일-소켓 해쉬맵은 서버에서 유저 소켓으로 채팅 메시지를 보낼 때 사용된다.
    public String makeJsonInfo(String msgType, String userEmail) {

        JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("msgType", msgType);

            jsonObject.put("userEmail", userEmail);

            //////아래 정보들은 필요 없는 정보이므로 공백처리/ 혹은 -1 처리함

            jsonObject.put("idGroup", -1);

            jsonObject.put("chatText", "");

            //채팅 접속자 이름
            jsonObject.put("userName", "");

            //채팅 접속자 프로필 사진 url
            jsonObject.put("userProfileUrl", "");

            //채팅 접속 시간
            jsonObject.put("chatTime", "");


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return jsonObject.toString();
    }

    // 형식화된 날짜를 얻는 함수.
    //formattedDate(new Date(), "a h:mm"); 를 하면 "오후 11:30" 형식의 데이터를 얻는다.
    public String formattedDate(Date date, String format) {
        SimpleDateFormat toFormat = new SimpleDateFormat(format);
        return toFormat.format(date);
    }


    //채팅을 종료하는 메소드
    public void exitChat() {


        try {
            //소켓의 스트림이 널이 아닐 때 사용 가능
            if (socket.getOutputStream() != null) {

                // jsonObject 에 채팅 종료 메세지를 세팅한다.
                String jsonMsg = makeJsonMsg("exitChat", userName + " 님이 그룹 채팅에서 나갔습니다.");


                //채팅 아이템 리스트를 초기화한다.
                ChatTextContent.ITEMS.clear();

                //로컬 db 에서 현재 채팅방의 채팅 내역을 삭제한다.
                removeChatHistory(idGroup);


                if (!socket.isClosed()) {
                    //소켓에서 아웃풋 스트림을 가져오고, 스트림을 통해 json 채팅 나감 메시지를 서버로 전달
                    outPutStream.writeUTF(jsonMsg);
                }


                //receive 스레드를 정지시킨다. -> 수정 필요.
                //
//                client.receive.interrupt();

                //
//                socket.close();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //채팅 종료 메시지를 보내는 메소드. 유저를 강퇴시켰을 때 사용
    public void setExitChatMsg(String jsonMsg){

        if (!socket.isClosed()) {
            //소켓에서 아웃풋 스트림을 가져오고, 스트림을 통해 json 채팅 나감 메시지를 서버로 전달
            try {
                if (socket.getOutputStream() != null) {


                    if (!socket.isClosed()) {
                        //소켓에서 아웃풋 스트림을 가져오고, 스트림을 통해 json 채팅 나감 메시지를 서버로 전달
                        outPutStream.writeUTF(jsonMsg);
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //클라이언트의 소켓을 초기화하여, 서버와 연결하는 asyncTask.
    public class InitSocketTask extends AsyncTask<Integer, String, String> {


        @Override
        protected String doInBackground(Integer... integers) {

            //서비스가 생성될 때 소켓을 초기화한다.
            try {
                socket = new Socket(ipText, port);

                //inputStream = socket.getInputStream();

                //소켓에서 아웃풋스트림 생성
                outPutStream = new DataOutputStream(socket.getOutputStream());
                inputStream = new DataInputStream(socket.getInputStream());

                //서버로부터 메세지를 받기 위한 스레드 생성
                receive = new ReceiveThread(socket);

                // 연결후 바로 ReceiveThread 시작
                receive.start();


                // 소켓 초기화가 완료되면, 해당 유저의 소켓 정보를 서버로 보낸다.
                // 소켓 정보는 서버의 유저이메일-소켓 해쉬맵에 저장된다.
                sendUserInfo();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

    }
}
