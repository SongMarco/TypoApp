package nova.typoapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import java.util.HashSet;
import java.util.Set;

import butterknife.ButterKnife;
import nova.typoapp.newsfeed.NewsFeedContent;

import static android.view.View.GONE;





//본 액티비티는 프래그먼트 3개를 품은 액티비티이다.
//프래그먼트를 getItem으로 호출한다.
//이 때 액티비티에 인터페이스를 적용하는 것을 잊지말라.
public class MainActivity extends AppCompatActivity
        implements
        NewsFeedFragment.OnListFragmentInteractionListener,
        WebFragment.OnFragmentInteractionListener,
        BlankFragment.OnFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    public static FloatingActionButton fabAdd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);



        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        fabAdd = (FloatingActionButton)findViewById(R.id.fabAdd);

        fabAdd.setVisibility(GONE);

//        // 뷰페이저에 onPageChangeL달아서 특정 페이지에서만 글쓰기 버튼이 보이게 하자.
//        mViewPager.addOnPageChangeListener( new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//               switch(position){
//
//
//                   case 0:
//
//                       fabAdd.setVisibility(View.VISIBLE);
//                       break;
//
//                   case 1:
//                       fabAdd.setVisibility(View.GONE);
//                       break;
//
//                   case 2:
//                       fabAdd.setVisibility(View.GONE);
//                       break;
//
//               }
//            }
//        });
//
//        Toast.makeText(this, "email = "+email+" name = "+name+" birthday = "+birthday, Toast.LENGTH_SHORT).show();



//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //툴바 옵션아이템 선택시 이벤트
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        Intent intent;

        switch(id){
            case R.id.action_settings:

                Toast.makeText(this, "설정을 클릭함", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_profile:


                intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);

                break;


            case R.id.action_logout:
                SharedPreferences prefLogin = getSharedPreferences( getString(R.string.key_pref_Login) , Activity.MODE_PRIVATE);
                Set<String> preferences = prefLogin.getStringSet("Cookie" , new HashSet<String>() );
                //페이스북 로그아웃처리하기.
                if (AccessToken.getCurrentAccessToken() != null) {


                    Toast.makeText(MainActivity.this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();


                    LoginManager.getInstance().logOut();

                    intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);

                    finish();

                }
                else if( !preferences.isEmpty() ){

                    Toast.makeText(MainActivity.this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

//                    LoginToken = false;

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

    @Override
    public void onListFragmentInteraction(NewsFeedContent.FeedItem item) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);


            return rootView;
        }
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {

                case 0:


                    return new NewsFeedFragment();


                case 1:
                    return new WebFragment();


                case 2:
                    return new BlankFragment();


                default:
                    return null;
            }

        }

        @Override
        public int getCount() {


            // Show 3 total pages.
            return 3;
        }
    }




    @Override
    protected void onPause() {
        super.onPause();



    }
}
