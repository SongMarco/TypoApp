package nova.typoapp.group;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import nova.typoapp.R;
import nova.typoapp.groupChat.ChatTextContent.ChatItem;
import nova.typoapp.groupChat.GroupChatFragment;
import nova.typoapp.groupChat.GroupChatService;
import nova.typoapp.groupChat.groupChatSqlite.MySQLiteOpenHelper;
import nova.typoapp.groupMember.GroupMemberContent;
import nova.typoapp.groupWordSet.GroupWordSetFragment;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.group.GroupInfoFragment.isMemberGroup;
import static nova.typoapp.retrofit.ApiService.API_URL;


//그룹 액티비티 - 그룹 프래그먼트에서 그룹을 클릭했을 때 나타나는 액티비티.
// 그룹 관련 활동을 진행
// 그룹 정보, 그룹 단어장, 그룹 채팅 기능 사용 가능
public class GroupActivity extends AppCompatActivity implements
        GroupInfoFragment.OnFragmentInteractionListener,
        GroupWordSetFragment.OnFragmentInteractionListener,
        GroupChatFragment.OnFragmentInteractionListener {


    //페이저 어댑터는 뷰페이저에 사용되는 어댑터이다. 각 프래그먼트(그룹 정보 / 그룹 단어장 / 그룹 채팅) 객체를 불러올 때 사용한다.
    private SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

    public static boolean isDeleteMemberMode = false;


    int idGroup; // 방의 id
    ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        ButterKnife.bind(this);

        /*
        툴바와 탭 레이아웃, 뷰페이저가 세팅된다.
        뷰페이저에 필요한 fragment 가 와서 표시된다.
         */


        //먼저 툴바 세팅
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarGroup);
        setSupportActionBar(toolbar);

        String nameGroup = getIntent().getStringExtra("nameGroup");
        getSupportActionBar().setTitle(nameGroup);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //탭 레이아웃 초기화
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayoutGroup);
        mViewPager = (ViewPager) findViewById(R.id.ViewPagerGroup);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //아래 두 줄을 다 추가하지 않으면 탭 레이아웃에 뷰페이저가 제대로 세팅되지 않는다 - 개선 여지 있음.
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        //
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



        //인텐트에서 채팅으로 가라는 변수가 세팅되어있다면 -> 그룹 채팅으로 세팅해줌
        if (getIntent().getBooleanExtra("goChatFragment", false)) {

            mViewPager.setCurrentItem(2);
        }


        //그룹의 멤버인지 확인하는 스태틱 변수 세팅
        isMemberGroup = getIntent().getBooleanExtra("isMemberGroup", false);


        //인텐트에서 그룹 id를 가져온다.
        idGroup = getIntent().getIntExtra("idGroup", -1);


    } //end of onCreate









    //채팅 내역 db를 불러와서 채팅 참여 여부를 판단하는 메소드
    public boolean isJoinChat() {

        String dbName = getResources().getString(R.string.chatdb_name); //db 이름. 브라우저에서 조회시 해당 파일명으로 DB 조회 가능(Stetho 등)
        int dbVersion = getResources().getInteger(R.integer.chatdb_version); // 데이터베이스 버전 : 앱이 바뀌면서 데이터베이스를 업그레이드할 때 필요.(DB 버전 관리)

        SQLiteDatabase dbChat; // db 객체

        //로컬 DB(SQLite) 의 초기화
        // 채팅 db를 가져오는데 필요한 SQLite OpenHelper 객체를 초기화
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(
                this,  // 현재 화면의 제어권자
                dbName,// db 이름
                null,  // 커서팩토리-null : 표준커서가 사용됨
                dbVersion);       // 버전

        //openHelper 를 이용, 채팅 내용 db를 가져온다.
        dbChat = helper.getWritableDatabase(); // 읽고 쓸수 있는 DB

        int idGroupFromIntent = getIntent().getIntExtra("idGroup", -1);

        //채팅방의 채팅 내역을 db 에서 가져온다. idGroup 을 기준으로 채팅방을 구분하였다.
        Cursor cursor = dbChat.rawQuery("select * from chatTable where idGroup = " + idGroupFromIntent + ";", null);

        //불러온 채팅 내역으로부터 채팅 아이템을 추가할 아이템 리스트를 만든다.
        List<ChatItem> cloneItems = new ArrayList<>();

        while (cursor.moveToNext()) {

            //채팅 아이템으로부터 db에 저장할 변수들을 가져온다.
            int idGroup = cursor.getInt(1); //그룹(채팅방) id
            int chatType = cursor.getInt(2); //채팅 메세지 타입 : 일반 채팅 / 공지사항(누가 들어오거나 나갔을 때) 구별
            String chatText = cursor.getString(3); // 채팅 메세지 내용
            String chatWriterName = cursor.getString(4); // 메세지 발신자 이름
            String chatWriterEmail = cursor.getString(5); // 메세지 발신자 이메일
            String chatWriterProfile = cursor.getString(6);// 메세지 발신자 프로필 url
            String chatTime = cursor.getString(7); // 메세지 발송 시간

            ChatItem loadChatItem = new ChatItem(idGroup, chatType, chatText, chatWriterName, chatWriterEmail, chatWriterProfile, chatTime);

            cloneItems.add(loadChatItem);
        }
        cursor.close();


        //        채팅 내역이 불러와짐. -> 채팅 방에 참여중 -> 채팅 내용 세팅 후 소켓 연결. 채팅모드로 사용
        if (cloneItems.size() != 0) {


            //채팅에 참여해있으므로 true 반환
            return true;

        }
        //채팅에 참여한 게 아니므로, false 반환
        else return false;


    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    //리사이클러뷰 갱신을 위해 뷰페이저를 이동함. 그룹원 강퇴시 사용
    public void notifyFragmentChanged(){

        mSectionsPagerAdapter.groupInfoFragment.getGroupMembersAfterBan();

    }


    /*
      뷰페이저에 붙이는 어댑터를 정의한다.
      뷰페이저에 어댑터를 붙이면 notifyDataSetChaged 같은 메소드를 통해
      뷰페이저를 새로고침 할 수 있다.
       */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }


        /*
        겟아이템 메소드

        사용자가 페이지 탭을 누르면 프래그먼트를 가져다주는 메소드.
        이 때 새로운 프레그먼트를 가져다 주기 때문에, 화면이 요소들이 갱신된다.
        (화면 요소는 댓글 수, 프로필 이미지, 게시물 등이 있다.)

         */

        //그룹 정보
        GroupInfoFragment groupInfoFragment = new GroupInfoFragment();

        //그룹 단어장
        GroupWordSetFragment groupWordSetFragment = new GroupWordSetFragment();

        //그룹 채팅
        GroupChatFragment groupChatFragment = new GroupChatFragment();

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {

                case 0:
                    return groupInfoFragment;


                case 1:
                    return groupWordSetFragment;


                case 2:
                    return groupChatFragment;


                default:
                    return null;
            }

        }

        // 페이지 갯수를 얻는 메소드.
        // @@@ 탭을 추가하려는 경우 이 값을 바꿔야 한다.
        // 갯수가 틀릴 경우 페이지가 제대로 표시되지 않는다.
        @Override
        public int getCount() {


            // Show 3 total pages.
            return 3;
        }
    }


    /*
   더보기 메뉴

   더보기 버튼을 클릭할 때 등장하는 메뉴들이다.
   프로필, 설정, 로그아웃 아이템이 표시된다.(필요시 추가 가능)
    */

    Menu varMenu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group, menu);
        varMenu = menu;
        return true;
    }

    //외부 클래스에서 onPrepareOptionsMenu 콜할 때 사용
    public void callOnPrepare(){
        onPrepareOptionsMenu(varMenu);

    }

    //채팅 참여 여부를 판단하여 채팅 나가기 버튼/그룹 탈퇴 버튼을 세팅한다.
    public boolean onPrepareOptionsMenu(Menu menu) {


        /*채팅 나가기 버튼 세팅*/

        //채팅 나가기 메뉴 아이템을 가져온다
        MenuItem miExitChat = menu.findItem(R.id.action_exit_chat);
        //유저가 채팅에 참여했다면
        if (isJoinChat()) {
            miExitChat.setVisible(true);
        }
        //유저가 채팅에 참여하지 않았다면
        else {
            miExitChat.setVisible(false);
        }


        /*그룹 탈퇴 버튼 세팅*/


        //그룹 탈퇴 버튼 아이템을 가져온다.
        MenuItem miLeaveGroup = menu.findItem(R.id.action_leave_group);

        //유저가 회원이라면
        if (isMemberGroup) {

            //그룹 탈퇴 버튼을 세팅
            miLeaveGroup.setVisible(true);

        }
        //회원이 아니라면 그룹탈퇴 버튼이 보이지 않음
        else {

            miLeaveGroup.setVisible(false);

        }

        /* 멤버 삭제 및 그룹장 위임 버튼 세팅*/

        //멤버 강퇴, 그룹장 위임 아이템을 가져온다.
        MenuItem miDelMember = menu.findItem(R.id.action_delete_member);

//        MenuItem miGiveAdmin = menu.findItem(R.id.action_give_admin);

        //유저가 운영자라면
        if ( isAdmin() ) {

            //멤버 강퇴, 그룹장 위임 아이템을 세팅
            miDelMember.setVisible(true);
//            miGiveAdmin.setVisible(true);
        }
        //운영자가 아니라면
        else {
            //멤버 강퇴, 그룹장 위임 아이템을 세팅하지 않음
            miDelMember.setVisible(false);
//            miGiveAdmin.setVisible(false);

        }



        //회원 삭제모드라면 삭제 완료 버튼을 세팅
        MenuItem miDeleteDone = menu.findItem(R.id.action_delete_done);
        if(isDeleteMemberMode){


            miDeleteDone.setVisible(true);

        }
        else{
            miDeleteDone.setVisible(false);
        }



        //알림을 거부한 방이라면 알림 거부->허용 메뉴를 세팅


        //알림 거부한 방 목록이 필요하다.
        SharedPreferences pref_notification = getSharedPreferences(getString(R.string.key_pref_notification), Activity.MODE_PRIVATE);

        //현재 방 id를 가져온다.
        int idGroup = getIntent().getIntExtra("idGroup", -1);

        //쉐어드 프리퍼런스에서 알림 거부된 방 id를 가져와본다.(거부하지 않았다면 -1 이 나옴)
        int refusedIdGroup = pref_notification.getInt(String.valueOf(idGroup), -1);


        //노티 관련 메뉴 가져옴
        MenuItem miNotiOff = menu.findItem(R.id.action_refuse_notification);
        MenuItem miNotiOn = menu.findItem(R.id.action_apply_notification);

        //현재 방이 거부된 방이 아니라면
        if( idGroup != refusedIdGroup ) {

            //알림 허용->거부 메뉴를 세팅. 알림을 허용해라. - 채팅 서비스에서도 같은 로직으로 알림 여부를 체크한다.

            miNotiOff.setVisible(true);
            miNotiOn.setVisible(false);

        }
        //현재 방이 알림 거부된 방이라면
        else{

            //알림 거부->허용 메뉴를 세팅

            miNotiOff.setVisible(false);
            miNotiOn.setVisible(true);

        }


//        String userEmail = pref_login.getString("cookie_email", "null");
//
//        Editor editor =



        return true;
    }

    //뒤로가기 버튼을 눌렀을 때
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

          /*
    옵션 아이템을 클릭하면 취하는 행동

    더보기 메뉴에 나오는 프로필, 설정, 로그아웃 아이템을
    클릭하였을 때 취할 행동을 정의한다.

     */

        //알림 거부한 방 목록을 가져오고, 해당 방을 이 목록에 추가한다.
        SharedPreferences pref_notification = getSharedPreferences(getString(R.string.key_pref_notification), Activity.MODE_PRIVATE);

        //쉐어드 에디터 세팅
        Editor editor = pref_notification.edit();

        switch (item.getItemId()) {

            //채팅 나가기 메뉴 클릭
            case R.id.action_exit_chat:

                //프래그먼트에서 채팅을 종료하게 한다.

                mSectionsPagerAdapter.groupChatFragment.exitChat();


                break;

            //그룹 나가기 메뉴 클릭
            case R.id.action_leave_group:

                //그룹 탈퇴 로직 진행
                Toast.makeText(this, "그룹을 탈퇴합니다", Toast.LENGTH_SHORT).show();



                //본인의 그룹원 이메일이 필요하다. -> 셰어드 프리퍼런스의 로그인 정보에서 메일 주소를 가져온다.
                SharedPreferences pref_login = getSharedPreferences(getString(R.string.key_pref_Login), Activity.MODE_PRIVATE);
                String userEmail = pref_login.getString("cookie_email", "null");

                LeaveGroupTask leaveGroupTask = new LeaveGroupTask(idGroup, userEmail);
                leaveGroupTask.execute();


                break;


            //회원 삭제 버튼 클릭
            case R.id.action_delete_member:

                Toast.makeText(this, "회원 삭제", Toast.LENGTH_SHORT).show();

                isDeleteMemberMode = true;

                //멤버 리스트뷰를 다시 갱신한다. -> 어댑터에서 isDeleteMemberMode 값이 true 임을 확인하고 삭제버튼을 세팅한다.
                mSectionsPagerAdapter.groupInfoFragment.refreshMemberList();

                //메뉴를 새로 세팅하게 한다.
                onPrepareOptionsMenu(varMenu);



                break;

                //회원 삭제 완료 버튼 클릭
            case R.id.action_delete_done:

                Toast.makeText(this, "회원 삭제 완료", Toast.LENGTH_SHORT).show();

                isDeleteMemberMode = false;

                //멤버 리스트뷰를 다시 갱신한다. -> 어댑터에서 isDeleteMemberMode 값이 true 임을 확인하고 삭제버튼을 세팅한다.
                mSectionsPagerAdapter.groupInfoFragment.refreshMemberList();

                //메뉴를 새로 세팅하게 한다.
                onPrepareOptionsMenu(varMenu);

                break;


//            //그룹장 위임 버튼 클릭
//            case R.id.action_give_admin:
//
//                Toast.makeText(this, "그룹장 위임", Toast.LENGTH_SHORT).show();
//
//
//                break;


                //알림 허용->거부 버튼 클릭
            case R.id.action_refuse_notification:

                //알림을 거부한다
                Toast.makeText(this, "이 그룹의 채팅 알림을 받지 않습니다.", Toast.LENGTH_SHORT).show();


                //쉐어드에 해당 방 id 를 키와 같게 하여 저장
                editor.putInt(String.valueOf(idGroup), idGroup);


                editor.apply();

                //옵션 메뉴 갱신
                onPrepareOptionsMenu(varMenu);

                break;


            case R.id.action_apply_notification:

                //알림을 허용한다.
                Toast.makeText(this, "이 그룹의 채팅 알림을 받습니다.", Toast.LENGTH_SHORT).show();

                //쉐어드에 해당 방 id 를 가져와 제거
                editor.remove(String.valueOf(idGroup));

                editor.apply();

                //옵션 메뉴 갱신
                onPrepareOptionsMenu(varMenu);

                break;




            // 뒤로가기 메뉴 클릭
            case android.R.id.home:
                onBackPressed();

                break;






        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop() {
        super.onStop();

        isDeleteMemberMode = false;
    }

    //유저가 관리자인지 아닌지 확인하는 함수
    public boolean isAdmin(){

        //그룹 멤버의 어레이 리스트를 살핀다.
        for(int i = 0; i<GroupMemberContent.ITEMS.size(); i++){

            //현재 로그인 한 계정의 이메일을 가져온다.
            SharedPreferences pref_login = getSharedPreferences(getString(R.string.key_pref_Login), Activity.MODE_PRIVATE);
            String userEmail = pref_login.getString("cookie_email", "null");


            GroupMemberContent.MemberItem memberItem = GroupMemberContent.ITEMS.get(i);

            //현재 멤버 아이템이 로그인 한 멤버 아이템일 경우
            if(memberItem.emailMember.equals(userEmail) ){

                //등급이 그룹장이라면 -> true 반환
                if(memberItem.levelMember == 3){

                    return true;
                }
                //그룹장이 아니면 -> false 반환
                else return false;


            }

        }

        return false;
    }



    //그룹을 탈퇴하는 태스크 - 회원 스스로 탈퇴할 때, 그룹장이 회원을 강퇴시킬 때 사용
    public class LeaveGroupTask extends AsyncTask<Void, Void, Void> {

        int idGroup;
        String memberEmail;
        Context mContext = GroupActivity.this;

        //방나감 처리를 위해 서비스와 연결 필요
        GroupChatService chatService;

        String json_result;

        //파라미터로 그룹 id, 멤버 이메일을 받는다.


        public LeaveGroupTask(int idGroup, String memberEmail) {
            this.idGroup = idGroup;
            this.memberEmail = memberEmail;
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


            //그룹 탈퇴가 이루어졌으므로,

            //멤버 가입 여부 변수를 false 로 한다.
            isMemberGroup = false;


            //GroupChatFragment(채팅방 나감 처리) 의 업데이트가 필요하다.
            // GroupInfoFragment(그룹 멤버 리스트 갱신),

            //채팅방에 참여중이었다면 채팅방에서 나가게 한다.
            if(isJoinChat()){

                //채팅 종료 처리하기 전에, 서비스에 바인드를 해야 한다.

                //groupChatFragment onResume 호출을 위해 페이지를 한 번 바꾼다.
                mViewPager.setCurrentItem(2);
                mViewPager.setCurrentItem(0);


                //채팅 프래그먼트의 onResume 을 콜하여, 서비스에 바인드시킨다.
                mSectionsPagerAdapter.groupChatFragment.idGroupStatic = idGroup;

//                //채팅 프래그먼트의 onResume 을 콜하여, 서비스에 바인드시킨다.
//                mSectionsPagerAdapter.groupChatFragment.onResume();

                //채팅프래그먼트에서 채팅을 종료 처리한다.
                mSectionsPagerAdapter.groupChatFragment.exitChat();

                //채팅 버튼을 감추게 한다.
                mSectionsPagerAdapter.groupChatFragment.hideChatStartBtn();


            }
            //채팅 참여를 안한 경우 -> 버튼만 감춘다.
            else{
                //groupChatFragment onResume 호출을 위해 페이지를 한 번 바꾼다.
                mViewPager.setCurrentItem(2);
                mViewPager.setCurrentItem(0);


                mSectionsPagerAdapter.groupChatFragment.onResume();
                mSectionsPagerAdapter.groupChatFragment.hideChatStartBtn();
            }



            //GroupInfoFragment 를 페이저 어댑터를 통해 가져온다.
            GroupInfoFragment groupInfoFragment = mSectionsPagerAdapter.groupInfoFragment;

            //GroupInfoFragment 에서 멤버 리스트를 갱신하고, 가입 버튼을 보이게 한다.
            groupInfoFragment.getGroupMembersAfterLeave();

//            //그룹 채팅 프래그먼트에서 채팅 시작 버튼을 안 보이게 한다.
//            GroupChatFragment groupChatFragment = mSectionsPagerAdapter.groupChatFragment;
//            groupChatFragment.setChatStartBtn();





        }


    }


}
