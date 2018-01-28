package nova.typoapp.group;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import nova.typoapp.R;
import nova.typoapp.groupWordSet.GroupWordSetFragment;

public class GroupActivity extends AppCompatActivity implements

        GroupInfoFragment.OnFragmentInteractionListener,
        GroupWordSetFragment.OnFragmentInteractionListener,
        GroupChatFragment.OnFragmentInteractionListener {


    //페이저 어댑터는 뷰페이저에 사용되는 어댑터이다. 각 프래그먼트 객체를 불러올 때 사용한다.
//            위의 기본 주석에 따르면, FragmentPagerAdapter 어댑터는 모든 페이지를 메모리에 올려두는데,
//            메모리가 부족해질 경우 FragmentStatePagerAdapter 어댑터를 쓰라고 언급하고 있다.
    //아래의 SectionsPagerAdapter 어댑터는 메인 액티비티에서 FragmentPagerAdapter 를 상속하여
    //만든 커스텀 어댑터다.
    private SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

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

        getSupportActionBar().setTitle("그룹");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //탭 레이아웃 초기화
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayoutGroup);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.ViewPagerGroup);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //아래 두 줄을 다 추가하지 않으면 탭 레이아웃에 뷰페이저가 제대로 세팅되지 않는다 - 개선 여지 있음.
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        //
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //아래 코드 대신 윗줄의 코드 추가함 -> 탭을 누를 때 반응시키기 위해.
//        tabLayout.setupWithViewPager(mViewPager);



    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }


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
//        @Override
//        public int getItemPosition(Object object) {
//
////            Toast.makeText(MainActivity.this, "getItemPos Called", Toast.LENGTH_SHORT).show();
//            Log.e("refresh", "getItemPosition: getItemPos called");
//
//            //리프레시 하는 경우 : 댓글 달고 올때, 프로필 고쳤을 때, 수정삭제했을때
//
//            return POSITION_NONE;
//
////            //댓글을 달러 왔으니까 리프레시 하지 말기
////            if(NewsFeedFragment.isWentCommentActivity ){
////                NewsFeedFragment.isWentCommentActivity = false;
////                return super.getItemPosition(object);
////            }
////            //그게 아니지만 프로필을 고치지 않았따면 리프레시 하지말기
////            else if ( !ProfileActivity.isProfileEdited ){
////
////                return super.getItemPosition(object);
////            }
////            //아이템을 삭제하지 않았다면 리프레시 하지말기
////
////            else{
////                ProfileActivity.isProfileEdited = false;
////                isItemDeleted = false;
////                return POSITION_NONE;
////            }
//
//
//        }


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

    //뒤로가기 버튼을 눌렀을 때
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

}
