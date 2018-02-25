package nova.typoapp.groupChat;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import nova.typoapp.R;
import nova.typoapp.groupChat.ChatTextContent.ChatItem;
import nova.typoapp.groupChat.groupChatSqlite.MySQLiteOpenHelper;
import nova.typoapp.groupChat.ottoEventBus.BusProvider;
import nova.typoapp.groupChat.ottoEventBus.ChatRcvEvent;

import static nova.typoapp.groupChat.ChatTextContent.CHAT_NOTICE;
import static nova.typoapp.groupChat.GroupChatFragment.idGroupStatic;


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
* 필요한 것들
*
* 소켓 생성 / 파기는 참여한 방의 갯수를 기준으로 이뤄진다.(구현 예정)
* 참여한 방이 0개 -> 1개로 될 경우, 소켓을 새로 만든다.
* 반대로 1개 -> 0개로 될 경우 소켓을 파기한다.
*
*
* @@ 채팅 서비스의 흐름
*
* 채팅 프래그먼트에서 채팅 시작 버튼을 누른다 -> 채팅 서비스 바인드 후 채팅 시작(소켓이 없을 경우 소켓을 생성)
*
* 채팅 진행 : 서비스에서 계속 소켓 통신, 채팅 프래그먼트 ui(채팅 리사이클러뷰) 업데이트 진행(otto 이벤트 버스 사용)
*
* 채팅 나감 : 서비스에서 해당 방에 대한 소켓 통신 종료, 참여한 방의 갯수 -1;
*
* 채팅 부재(화면 전환, onStop) : 서비스에서 계속 소켓 통신.
*
*
*
* 특이사항 / 의문사항
*
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

    private static final String ipText = "192.168.242.1"; // tcp 소켓을 연결할, 서버의 ip - 내부 ip. 테스트용
    //        private static final String ipText = "115.68.231.13"; // tcp 소켓을 연결할, 서버의 ip
    private static int port = 9999; // 채팅 서버의 포트

    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;


        // 그룹챗 프래그먼트에서 서비스 바인딩이 일어난다.
        return mBinder;
    }


    /*서비스가 언바인드될 때
    * case 1 : 채팅방 프래그먼트에서 다른 화면으로 갔을 때때
    *
    * */
    @Override
    public boolean onUnbind(Intent intent) {

        this.idGroup = -1;

        return super.onUnbind(intent);

    }

    //바인더 클래스
    class ChatBinder extends Binder {
        //바인더에서 서비스를 가져오는 메소드
        GroupChatService getService() {
            return GroupChatService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        Log.d("test", "서비스의 onCreate");
//        android.os.Debug.waitForDebugger();  // this line is key

        //서비스가 만들어질 때, 소켓을 초기화하여 서버와 연결한다.
        //메인 스레드에서 소켓을 만들 수 없으므로, asyncTask 로 소켓 초기화(InitSocketTask 클래스에 ctrl+b를 눌러 참조)
        new InitSocketTask().execute();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.d("test", "서비스의 onStartCommand");

        return super.onStartCommand(intent, flags, startId);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행 -> 서비스는 어지간하면 죽지 않는데, 안드로이드에서 앱을 죽일 때 죽게됨

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

    //소켓이 생성될 때 서버에 유저 정보를 보내는 메소드
    //서비스가 죽었다가 살아났을 때(앱 관리자에서 종료), 서버에 있는 채팅방에서 유저의 소켓을 갱신하기 위해 사용한다.

    //모든 방을 조회해서 유저 소켓을 바꿀 필요가 없다.
    //서버에 유저-소켓 관계의 해쉬맵을 추가하고, 소켓 연결시 이를 추가한 뒤,
    //연결 되지 않은 유저 소켓이 있을 때 이 해쉬맵을 조회하여 수정하면 된다.
    public void sendUserInfo(){
        try {

            //채팅 방에 접속한 유저들에게 사용자가 채팅방에 접속했다는 메세지 전송

            //보낼 json 메시지의 타입 -> 채팅방 접속인지, 나간 건지, 채팅인지 구별할 때 사용
            String msgType = "userInfo";


            //sharedPref 에서 유저의 이메일 정보를 가져온다.

            //셰어드에서 로그인토큰을 가져온다.
            SharedPreferences pref_login = getSharedPreferences(getString(R.string.key_pref_Login) , 0);
            userEmail = pref_login.getString("cookie_email","");

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

                    //todo 현재 메세지를 수신한 채팅방에서 채팅 중이 아니라면(백그라운드 상태) -> Fcm  알림을 사용자에게 전달한다.
                    //if(백그라운드 상태) -> 채팅방의 채팅 fcm 알림 전달

                    //채팅방 화면의 그룹 id 값 != 수신한 메시지의 id값 인 경우 -> 알림 전달 필요.
                    if(idGroupStatic != chatItem.idGroup){

                        //클라이언트에 알림을 전달한다.



                    }
                    //채팅방 화면의 그룹 id 값 == 수신한 메시지의 id값 인 경우 -> 알림 전달 필요없고, ui 업데이트 필요함.
                    else{
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
    // 유저 이메일-소켓 해쉬맵은 유저 소켓으로 메시지를 못 보낼 때(앱을 강제종료했을 때) 사용된다.
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

    //클라이언트의 소켓을 초기화하여, 서버와 연결하는 asyncTask.
    public class InitSocketTask extends AsyncTask<Integer, String, String> {


        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.


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
