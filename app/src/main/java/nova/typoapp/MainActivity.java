package nova.typoapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import java.util.HashSet;
import java.util.Set;

import nova.typoapp.group.GroupFragment;
import nova.typoapp.newsfeed.NewsFeedContent;
import nova.typoapp.notificationlist.NoticeContent;
import nova.typoapp.notificationlist.NoticeItemFragment;
import nova.typoapp.wordset.WordSetContent;
import nova.typoapp.wordset.WordSetListFragment;


/*
메인 액티비티

메인 액티비티는 뷰페이저로 구성되어, 한 액티비티에 여러 화면이 Fragment 형태로 담겨져 있다.

각 페이지를 상단의 탭을 눌러 이동할 수 있다.
우측 상단의 더보기 버튼(...모양)을 클릭하면 프로필과 설정 화면으로 이동할 수 있다.

페이지는 총 3페이지로 되어 있다.
1페이지는 뉴스피드 페이지이다. 사용자가 단어를 등록하고, 댓글과 좋아요로 소통할 수 있다.
2페이지, 3페이지는 미구현이다. 2페이지는 단어장 컨텐츠, 3페이지는 게임을 구현할 예정이다.(변경 가능)

페이지 이동으로 프래그먼트를 가져올 때에는, getItem으로 호출한다.
이 때 액티비티에 인터페이스(OnListFragmentInteractionListener)를 적용하는 것을 잊지말라.

*/
public class MainActivity extends AppCompatActivity
        implements
        NewsFeedFragment.OnListFragmentInteractionListener,
        WebFragment.OnFragmentInteractionListener,
        BlankFragment.OnFragmentInteractionListener,
        NoticeItemFragment.OnListFragmentInteractionListener,
        WordSetListFragment.OnListFragmentInteractionListener,
        GroupFragment.OnFragmentInteractionListener

{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    //페이저 어댑터는 뷰페이저에 사용되는 어댑터이다. 각 프래그먼트 객체를 불러올 때 사용한다.
//            위의 기본 주석에 따르면, FragmentPagerAdapter 어댑터는 모든 페이지를 메모리에 올려두는데,
//            메모리가 부족해질 경우 FragmentStatePagerAdapter 어댑터를 쓰라고 언급하고 있다.
    //아래의 SectionsPagerAdapter 어댑터는 메인 액티비티에서 FragmentPagerAdapter 를 상속하여
    //만든 커스텀 어댑터다.
    private SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
    ;

    public static int feedIDFromFcm = -1;

    /*
    메인액티비티 초기화.

    뷰를 세팅할 때 버터나이프를 자주 사용하는데,
    메인 액티비티에서는 inflate 할 뷰가 적으므로 그냥 findViewById 적용한다.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        툴바와 탭 레이아웃, 뷰페이저가 세팅된다.
        뷰페이저에 필요한 fragment 가 와서 표시된다.
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        // Set up the ViewPager with the sections adapter.
        /*
        The {@link ViewPager} that will host the section contents.
         */

        //탭 레이아웃 초기화
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayoutMain);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

//        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabReselected(TabLayout.Tab tab) {


                if (tab.getPosition() == 0) {

//                Toast.makeText(context, "update called", Toast.LENGTH_SHORT).show();


                    //'영어단어' 페이지를 한 번 더 클릭할 경우, 맨 위로 스크롤해주는 기능이다.
                    //와! 이렇게 현재 프래그먼트를 가져올 수 있구나! - 새로고침에서도 써먹을 수 있겠어.
                    mSectionsPagerAdapter.newsFeedFragment.scrollToTop();

                }

            }
        });

        //만약 인텐트에서 feedID 값이 세팅된다면 알림을 통해 / 혹은 외부에서 접근한 것이다.
        //feedIDFromFcm 값이 -1이 아니게 될 것이고, 그렇게 되면 프래그먼트에서 해당 게시물부터 페이징이 가능해진다.
        //newFeedFragment 의 refresh Task 참고
        if (getIntent().getIntExtra("feedIDFromFcm", -1) != -1) {

            feedIDFromFcm = getIntent().getIntExtra("feedIDFromFcm", -1);
        }


    }

    public void updateWordSet() {


        mSectionsPagerAdapter.wordSetFragment.updateWordSet();
    }

    /*
    더보기 메뉴

    더보기 버튼을 클릭할 때 등장하는 메뉴들이다.
    프로필, 설정, 로그아웃 아이템이 표시된다.(필요시 추가 가능)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    /*
    옵션 아이템을 클릭하면 취하는 행동

    더보기 메뉴에 나오는 프로필, 설정, 로그아웃 아이템을
    클릭하였을 때 취할 행동을 정의한다.

     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        Intent intent;


        //선택한 아이템에서 id를 취하여, 조건문으로 아이템에 맞게 행동한다.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:


                intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);

                break;


            //설정 아이템 클릭 -> 설정 액티비티로 이동(미구현, 토스트만 표시)
            case R.id.action_settings:
                Toast.makeText(this, "설정을 클릭함", Toast.LENGTH_SHORT).show();
                break;

            // 프로필 아이템 클릭함 -> 프로필 액티비티로 이동
            case R.id.action_profile:


                intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);

                break;


            // 로그아웃 아이템 클릭함 -> 로그아웃 처리함
            case R.id.action_logout:

                //
                SharedPreferences prefLogin = getSharedPreferences(getString(R.string.key_pref_Login), Activity.MODE_PRIVATE);
                Set<String> preferences = prefLogin.getStringSet("Cookie", new HashSet<String>());


                //페이스북 로그인 한 계정 로그아웃처리하기.
                if (AccessToken.getCurrentAccessToken() != null) {


                    Toast.makeText(MainActivity.this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

                    LoginManager.getInstance().logOut();

                    intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);

                    finish();

                }

                //일반계정 로그아웃처리하기
                else if (!preferences.isEmpty()) {

                    Toast.makeText(MainActivity.this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

                    //                    LoginToken = false;

                  /*  로그아웃 처리를 위해, 로그인 정보를 담은
                    prefLogin을 가져오고, 로그인 정보를 클리어한다.*/
                    SharedPreferences.Editor editor = prefLogin.edit();

                    editor.clear();
                    editor.apply();


                    intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);

                    finish();
                }

                break;

            //noinspection SimplifiableIfStatement
        }

        return super.onOptionsItemSelected(item);
    }


    /*
    필수로 implement해야하는 메소드이며, 프래그먼트의
    의사소통에 사용된다.
     */
    @Override
    public void onListFragmentInteraction(NewsFeedContent.FeedItem item) {

    }

    @Override
    public void onListFragmentInteraction(NoticeContent.NoticeItem item) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
    @Override
    public void onListFragmentInteraction(WordSetContent.WordSetItem item) {

    }





    /*
    PlaceHolderFragment 는 빈화면에서 사용되는 프래그먼트이다.
    현재는 뷰페이저의 getItem 을 통해 프레그먼트를 가져오므로,

    해당 코드들을 주석처리해도 정상 작동함을 확인했다.
    만약의 경우를 위해 삭제하지는 않았다. 필요시 기능을 확인하고 삭제 후 테스트해보라.
     */

//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        public PlaceholderFragment() {
//        }
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//
//
//            return rootView;
//        }
//    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

    /*
    뷰페이저에 붙이는 어댑터를 정의한다.
    뷰페이저에 어댑터를 붙이면 notifyDatasetChaged같은 메소드를 통해
    뷰페이저를 새로고침 할 수 있다.

    뷰페이저를 마치 커스텀뷰처럼 쓸 수 있게 해주는 어댑터로 추정된다.
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
                겟아이템포지션 메소드

                뉴스피드 페이지를 새로고침 하도록 하는 메소드.
                onResume에서 notifyDatasetchanged가 콜되면,
                이 메소드가 콜되면서 페이지를 새로고치게 된다.

                원리는 사실 이 함수가 아이템의 위치를 주는데,
                뷰페이저가 아이템의 위치를 모르게 함으로써 모든 페이지를 새로 고치게 한다.

                합리적인 구조는 아니지만 구현이 간단하여 일단 구현해놨다. 리팩토링이 필요하다.

                 */
        @Override
        public int getItemPosition(Object object) {

//            Toast.makeText(MainActivity.this, "getItemPos Called", Toast.LENGTH_SHORT).show();
            Log.e("refresh", "getItemPosition: getItemPos called");

            //리프레시 하는 경우 : 댓글 달고 올때, 프로필 고쳤을 때, 수정삭제했을때

            return POSITION_NONE;

//            //댓글을 달러 왔으니까 리프레시 하지 말기
//            if(NewsFeedFragment.isWentCommentActivity ){
//                NewsFeedFragment.isWentCommentActivity = false;
//                return super.getItemPosition(object);
//            }
//            //그게 아니지만 프로필을 고치지 않았따면 리프레시 하지말기
//            else if ( !ProfileActivity.isProfileEdited ){
//
//                return super.getItemPosition(object);
//            }
//            //아이템을 삭제하지 않았다면 리프레시 하지말기
//
//            else{
//                ProfileActivity.isProfileEdited = false;
//                isItemDeleted = false;
//                return POSITION_NONE;
//            }


        }


        /*
        겟아이템 메소드

        사용자가 페이지 탭을 누르면 프래그먼트를 가져다주는 메소드.
        이 때 새로운 프레그먼트를 가져다 주기 때문에, 화면이 요소들이 갱신된다.
        (화면 요소는 댓글 수, 프로필 이미지, 게시물 등이 있다.)

         */

        NewsFeedFragment newsFeedFragment = new NewsFeedFragment();

        NoticeItemFragment noticeItemFragment = new NoticeItemFragment();

        WordSetListFragment wordSetFragment = new WordSetListFragment();

        //클래스 채팅방을 위한 프래그먼트
        GroupFragment groupFragment = new GroupFragment();

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {

                case 0:


                    Log.e("refresh", "getItem: called");
//                    return ListFragment.newInstance(position);
                    return newsFeedFragment;


                case 1:
                    return wordSetFragment;


                case 2:
                    return noticeItemFragment;

                case 3:
                    return groupFragment;


                default:
                    return null;
            }

        }



        // 페이지 갯수를 얻는 메소드.
        // @@@ 탭을 추가하려는 경우 이 값을 바꿔야 한다.
        // 갯수가 틀릴 경우 페이지가 제대로 표시되지 않는다.
        @Override
        public int getCount() {


            // Show 4 total pages.
            return 4;
        }
    }


    //@@@@@ 중요@@@@@ onResume에서 페이저 어댑터에 notifyDataSetChanged를 호출한다.
    //이것으로 프로필 사진이 고쳐진지 유무를 체크해서, 프로필 사진을 변경하게 된다.
    //이 때 getItemPosition이, mSectionPagerAdapter에서 notify 후에 콜된다.
    //getItemPosition의 반환값을 NONE 으로 주면 뷰페이저가 새로고침된다. (getItemPosition 메소드 참조)
    @Override
    public void onResume() {
        super.onResume();

////        Toast.makeText(this, "MainonResume called", Toast.LENGTH_SHORT).show();
//        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    //onResume 에서 페이저 어댑터를 notify 할 경우,
    //새로고침이 두번 되는 문제가 발생한다.
    /*
    액티비티가 안보였다가 다시 이어지는 과정에서는
    onRestart 가 타이밍이 맞는다.

    이렇게 하면 처음 생성될 때는 콜이 안 되고,
    나중에 다시 생길 때 콜이 된다.

     */
    @Override
    protected void onRestart() {
        super.onRestart();


        mSectionsPagerAdapter.notifyDataSetChanged();

    }

    public SectionsPagerAdapter getPagerAdapter() {

        return mSectionsPagerAdapter;

    }

//


    @Override
    protected void onPause() {
        super.onPause();

//        Toast.makeText(this, "onPause Called", Toast.LENGTH_SHORT).show();
    }
}
