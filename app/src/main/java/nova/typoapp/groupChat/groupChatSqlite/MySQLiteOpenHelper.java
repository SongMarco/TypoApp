package nova.typoapp.groupChat.groupChatSqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2018-02-24.
 */


//SQLite 초기화에 필요한 헬퍼 클래스.
//SQLite 초기화는 메인 액티비티에서 이루어진다.
//SQLite 로컬 db는 채팅 내역을 저장할 때 사용한다.

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    // 안드로이드에서 SQLite 데이터 베이스를 쉽게 사용할 수 있도록 도와주는 클래스
    public MySQLiteOpenHelper(Context context, String name,
                              CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // 데이터 베이스를 사용하려는데 데이터베이스가 없을경우, 데이터베이스 생성을 위해 호출됨
        // 테이블 생성하는 코드를 작성한다

        //채팅 아이템을 기준으로 테이블을 생성한다. 아래는 채팅 아이템의 내용물이다.
//      chatItem = new ChatItem(idGroup, chatType, chatText, userName, userEmail);

//        int idGroup = chatItem.idGroup;
//        int chatType = chatItem.chatItemType;
//        String chatText = chatItem.chatText;
//        String chatWriterName = chatItem.chatWriterName;
//        String chatWriterEmail = chatItem.chatWriterEmail;
//        String chatWriterProfile = chatItem.chatWriterProfile;
//        String chatTime = chatItem.chatTime;


        // 채팅 아이템 정보를 저장할 테이블을 생성한다.
        String sql = "create table chatTable(idChat integer primary key autoincrement, idGroup INTEGER, chatType INTEGER ," +
                " chatText TEXT, userName TEXT, userEmail TEXT, userProfileUrl TEXT,  chatTime TEXT);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 데이터베이스의 버전이 바뀌었을 때 호출되는 콜백 메서드
        // 버전 바뀌었을 때 기존데이터베이스를 어떻게 변경할 것인지 작성한다
        // 각 버전의 변경 내용들을 버전마다 작성해야함

        switch (oldVersion) {
            case 1:

                String sql = "drop table chatTable;"; // 테이블 드랍
                db.execSQL(sql);
                onCreate(db); // 다시 테이블 생성

                break;
        }
    }


}