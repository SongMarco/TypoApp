package nova.typoapp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import static nova.typoapp.LauncherActivity.LoginToken;

public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        switch(id){
            case R.id.action_settings:

                Toast.makeText(this, "설정을 클릭함", Toast.LENGTH_SHORT).show();
                break;

            case R.id.action_profile:

                Toast.makeText(this, "프로필을 클릭함", Toast.LENGTH_SHORT).show();
                break;


            case R.id.action_logout:

                //페이스북 로그아웃처리하기.
                if (AccessToken.getCurrentAccessToken() != null) {


                    Toast.makeText(MainActivity.this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();


                    LoginManager.getInstance().logOut();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);

                    finish();

                }
                else if(LoginToken){

                    Toast.makeText(MainActivity.this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();

                    LoginToken = false;

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);

                    finish();
                }






                break;






            //noinspection SimplifiableIfStatement
        }





        return super.onOptionsItemSelected(item);
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

            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            WebView mWebView = (WebView)rootView.findViewById(R.id.webView);
            WebSettings mWebSettings; //웹뷰세팅


            if(getArguments().getInt(ARG_SECTION_NUMBER) == 1){
                // 웹뷰 세팅
                //레이어와 연결
                mWebView .setWebViewClient(new WebViewClient()); // 클릭시 새창 안뜨게
                mWebSettings = mWebView.getSettings(); //세부 세팅 등록
                mWebSettings.setJavaScriptEnabled(true); // 자바스크립트 사용 허용

                mWebView.loadUrl("http://115.68.231.13/"); //원하는 URL  입력
            }
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));


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
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    public void saveLogin(){

        SharedPreferences prefLogin = getSharedPreferences( getString(R.string.key_pref_Login) , Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefLogin.edit();

        editor.putBoolean("LoginToken", LauncherActivity.LoginToken);
        Log.i("tokenSaved", String.valueOf(LoginToken));
        editor.apply();
    }


    @Override
    protected void onPause() {
        super.onPause();

        saveLogin();


    }
}
