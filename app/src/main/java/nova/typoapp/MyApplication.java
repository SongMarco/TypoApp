package nova.typoapp;

import android.app.Application;
import android.content.Intent;

import com.facebook.stetho.Stetho;

import nova.typoapp.groupChat.GroupChatService;

/**
 * Created by Administrator on 2017-12-04.
 */


public class MyApplication extends Application {

    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);

//        채팅 서비스 초기화 및 시작. - 채팅 서버와의 소켓 통신에 사용된다.
//        //자세한 내용은 groupChat 패키지의 groupChatService 참조
        Intent intent = new Intent(
                getApplicationContext(),//현재제어권자
                GroupChatService.class); // 이동할 컴포넌트
        startService(intent); // 서비스 시작
    }

}
