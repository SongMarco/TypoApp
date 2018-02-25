package nova.typoapp.groupChat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-02-04.
 */

/*

채팅텍스트의 리사이클러뷰를 위한 리스트

채팅 ui 를 리사이클러뷰로 구현하기 위해 만들었다.

채팅 텍스트 아이템과, 그 아이템의 리스트로 구성되어 있다.

 */
public class ChatTextContent {


    //채팅 아이템의 타입을 나타내는 상수. 일반적으로 CHAT_TEXT 값이며,
    //'~~가 나갔습니다' 등 공지를 나타낼 때 CHAT_NOTICE 가 쓰인다.
    public static int CHAT_TEXT = 100;

    public static int CHAT_NOTICE = 200;




    //채팅 텍스트 아이템의 리스트
    public static List<ChatItem> ITEMS = new ArrayList<>();



    //채팅 텍스트 한 개를 의미하는 아이템
    public static class ChatItem {

        //채팅한 그룹의 id - 채팅방 구별에 사용
        int idGroup;

        //채팅 아이템의 타입. 디폴트 값으로 CHAT_TEXT 설정하고,
        //공지일 때 수정
        int chatItemType = CHAT_TEXT;


        //채팅 내용
        String chatText;

        //채팅 작성자
        String chatWriterName;

        //작성자의 이메일
        String chatWriterEmail;

        //작성자의 프로필 이미지 url
        String chatWriterProfile;

        //채팅 내용이 작성된 날짜
        String chatTime;

        //조회수 : 일단 미구현
        int numSeen;

        public ChatItem(int idGroup, String chatText, String chatWriterName, String chatWriterEmail, String chatWriterProfile, String chatTime) {
            this.idGroup = idGroup;
            this.chatText = chatText;
            this.chatWriterName = chatWriterName;
            this.chatWriterEmail = chatWriterEmail;
            this.chatWriterProfile = chatWriterProfile;
            this.chatTime = chatTime;
        }

        public ChatItem(int idGroup, int chatItemType, String chatText, String chatWriterName, String chatWriterEmail) {
            this.idGroup = idGroup;
            this.chatText = chatText;
            this.chatItemType = CHAT_NOTICE;
            this.chatWriterName = chatWriterName;
            this.chatWriterEmail = chatWriterEmail;
        }

        public ChatItem(int idGroup, int chatItemType, String chatText, String chatWriterName, String chatWriterEmail, String chatWriterProfile, String chatTime) {
            this.idGroup = idGroup;
            this.chatItemType = chatItemType;
            this.chatText = chatText;
            this.chatWriterName = chatWriterName;
            this.chatWriterEmail = chatWriterEmail;
            this.chatWriterProfile = chatWriterProfile;
            this.chatTime = chatTime;
        }
    }
}
