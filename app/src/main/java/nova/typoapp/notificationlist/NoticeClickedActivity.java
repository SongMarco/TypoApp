package nova.typoapp.notificationlist;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import nova.typoapp.NewsFeedFragment;
import nova.typoapp.R;
import nova.typoapp.newsfeed.NewsFeedContent;


/*
알림을 클릭할 때 - 좋아요를 보고 들어오는 액티비티.

알림은 좋아요, 댓글, 답글이 있는데
각각 다르게 화면을 세팅하게 된다.

1. 좋아요
게시물 프래그먼트를 띄워서
게시물을 보여주게 된다.

이 때 게시물 프래그먼트는 영어단어 프래그먼트와 동일한 NewsFeedFragment 다.




 */


public class NoticeClickedActivity extends AppCompatActivity
        implements NewsFeedFragment.OnListFragmentInteractionListener
{

    public static int feedIDFromFcmInNoticeClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_clicked);

        //상단 툴바를 세팅한다.

        Toolbar toolbarNoticeFeed = (Toolbar) findViewById(R.id.toolbarNoticeFeed);

        setSupportActionBar(toolbarNoticeFeed);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        getSupportActionBar().setTitle("알림 게시물");

        feedIDFromFcmInNoticeClicked = getIntent().getIntExtra("feedIDFromFcm", -1);

        Log.e("feedIDFromFcm", "onCreate: "+feedIDFromFcmInNoticeClicked );

        NewsFeedFragment newsFeedFragment = (NewsFeedFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentNoticeClicked);

        newsFeedFragment.getFeedInfo();




    }


    /*
    좌측 상단의 뒤로가기 버튼을 세팅하기 위한 코드
    뒤로가기 버튼을 누르면, 이전 액티비티로 돌아가게 된다.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onListFragmentInteraction(NewsFeedContent.FeedItem item) {

    }
}
