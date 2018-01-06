package nova.typoapp.wordpuzzle;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.R;
import nova.typoapp.worditem.WordItemContent.WordItem;



/*
    액티비티 : WordPuzzleActivity

    1. 개요

    단어장에서 넘어오는 화면으로써,
    단어 퍼즐을 풀 며 학습할 수 있다.

    2. 구성

    a) 퍼즐 구성을 위한 리사이클러뷰


    3. 동작

    1) 퍼즐 초기화 하기

    단어장 액티비티에서 넘어올 때
    단어 아이템들이 담긴 리스트를 인텐트에서 받아 저장한다.

    단어 리스트에 어댑터와 리사이클러뷰를 세팅하여
    퍼즐을 초기화한다.

    2) 퍼즐 풀기

    퍼즐의 리사이클러뷰(이하 rv)에는 클릭 리스너가 지정돼 있다.

    rv의 아이템 두 개를 클릭하면 정답을 확인한다.

    정답이면 두 아이템이 소멸된다.(애니메이션)

    오답이면 벌칙으로 시간이 추가된다.


    4. 특이사항







 */





public class WordPuzzleActivity extends AppCompatActivity

        implements WordPuzzleStartFragment.OnFragmentInteractionListener,
        WordPuzzlePlayFragment.OnFragmentInteractionListener,
        WordPuzzleEndFragment.OnFragmentInteractionListener
{




    public static int countPickedCards = 0;
    public static int countCorrectItems = 0;


    //1초
    int timerSec;
    //0.1초
    int timerdeciSec;


    public static ArrayList<WordItem> listPickedItem= new ArrayList<>();


    public static ArrayList<MyWordPuzzleAdapter.ViewHolder> listPickedViewHolder = new ArrayList<>();



    @BindView(R.id.containerFragmentPuzzle)
    FrameLayout containerFragmentPuzzle;


    //퍼즐의 타이머를 위한 텍스트뷰. - 처음에는 보이지 않다가, 게임을 시작하면 보이게 된다.
    @BindView(R.id.tvPuzzleTime)
    TextView tvPuzzleTime;

    public static ArrayList<WordItem> gotItemsCopy;


    TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_puzzle);

        ButterKnife.bind(this);

        //상단 툴바를 세팅한다.
        Toolbar toolbarComment = (Toolbar) findViewById(R.id.toolbarWordPuzzle);

        setSupportActionBar(toolbarComment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("카드 매칭");


        //프래그먼트를 세팅한다.

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        WordPuzzleStartFragment startFragment = new WordPuzzleStartFragment();
        Bundle bundle = new Bundle();
        startFragment.setArguments(bundle);

        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.containerFragmentPuzzle, startFragment); // Activity 레이아웃의 View ID
        fragmentTransaction.commit();


        // 액티비티를 건너올 때 받은 아이템 리스트를 받아 저장한다.
        ArrayList<WordItem> gotItems = new ArrayList<>();
        gotItems = getIntent().getParcelableArrayListExtra("bundleItems");

        /*
        gotItems 의 사이즈가 9보다 크면, 9개가 되도록 줄여야 한다.(화면이 넘쳐버림)

        제외할 아이템을 랜덤하게 정하기 위해 셔플을 하고,
        subList 를 추출한다.
        gotItems
        */


        gotItemsCopy  = new ArrayList<>();







            if(gotItems.size() > 9){
                //리스트를 셔플한다.
                Collections.shuffle(gotItems);

                //리스트를 9개까지 추출한다. 여기서 subLIst(0,9)는 인덱스 0~8까지의 9개를 의미한다.(9는 제외됨에 유의, 자바 문법)

                gotItems = new ArrayList<WordItem>(gotItems.subList(0,9));

            }


            //gotItemsCopy 에 gotItem 을 deep Copy 한다. - 어레이 리스트와, 그 안에 있는 아이템 모두 딥 카피 해야함

            for(int i = 0; i < gotItems.size(); i++){

                gotItemsCopy.add(new WordItem(gotItems.get(i)) );

            }

            //단어 이미지와 뜻을 따로 세팅하기 위해 카피한 세트의 imgUrl은 비워둔다.
            // 이렇게 하면 어댑터에서 img url이 비었다면 단어의 텍스트를 보여준다.
            for(int i = 0; i < gotItems.size(); i ++){

                gotItemsCopy.get(i).UrlWordImg = "";

            }

            gotItemsCopy.addAll(gotItems);

//        Log.e("wow", "onCreate: after Shuffle ######" );


            Collections.shuffle(gotItemsCopy);

            for(int i = 0; i < gotItems.size(); i ++){

                gotItemsCopy.get(i).getItemInfo();

            }



            //static 한 아이템 어레이를 만들었다. 이 변수는 액티비티에서 나가면 초기화되며,
            //액티비티에 들어오면 미리 세팅 되있다가, 퍼즐을 플레이할 때 어댑터에 세팅된다.
            // static ㅎ ㅏ므로 초기화로 인한 오류가 없는지 확인할 것.












    }


    public void setCopyList(){



    }




    //화면을 벗어나면 스태틱 변수들을 초기화한다.

    @Override
    protected void onStop() {
        super.onStop();



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        //액티비티를 나가면 관련 스태틱 변수들 초기화

//        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
        WordPuzzleActivity.countCorrectItems = 0;
        WordPuzzleActivity.countPickedCards = 0;
        WordPuzzleActivity.listPickedItem.clear();
        WordPuzzleActivity.listPickedViewHolder.clear();
//        WordPuzzleActivity.gotItemsCopy.clear();

        stopTimer();




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
    public void onBackPressed() {
        super.onBackPressed();


    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void runTimer(){

        startTimer();

    }

    public void stopTimer(){

        if(stopwatchTimer != null){


            stopwatchTimer.cancel();
            stopwatchTimer.purge();
            stopwatchTimer = null;
        }

    }



    Timer stopwatchTimer;

    public long startTime = 0;
    public void startTimer() {
        stopwatchTimer = new Timer();

        startTime = System.currentTimeMillis();
        stopwatchTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView timerTextView = (TextView) findViewById(R.id.tvPuzzleTime);
                        timerTextView.setText(stopwatch());
                    }
                });

            }
        }, 0, 10);
    }

    // Returns the combined string for the stopwatch, counting in tenths of seconds.
    public String stopwatch() {
        long nowTime = System.currentTimeMillis();
        long cast = nowTime - startTime;
        Date date = new Date(cast);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("s.S");
        return simpleDateFormat.format(date);
    }
//
//
//    public class TimerTask extends AsyncTask<String, String, String> {
//
//        //onPre 에서 타이머 변수를 초기화
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//
//            Toast.makeText(WordPuzzleActivity.this, "running", Toast.LENGTH_SHORT).show();
//            timerdeciSec = 0;
//            timerSec = 0;
//
//
//        }
//
//
//        //타이머가 정지될 때까지 타이머를 돌림.
//        @Override
//        protected String doInBackground(String... params) {
//            while (!isCancelled() ) {
//
//                    //0.1초를 대기한다
//                    try {
//                        Thread.sleep(100);
//                    } catch (Exception e1) {
//                        System.out.println(e1);
//                    }
//
//                    timerdeciSec++;
//                    //0.1초가 지났으니 0.1초를 증가시킨다.
//
//                    //1초단위가 되면 0.1초 단위를 0으로 하고, 초를 증가시킨다.
//                    if (timerdeciSec == 10) {
//                        timerdeciSec = 0;
//                        timerSec++;
//                    }
//
//                    this.publishProgress(timerSec+"."+timerdeciSec);
//
//            }
//
//            return timerSec+"."+timerdeciSec;
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            super.onProgressUpdate(values);
//            TextView tvTimer = (TextView)findViewById(R.id.tvPuzzleTime);
//            tvTimer.setText(values[0]);
//        }
//
//        @Override
//        protected void onCancelled() {
//            super.onCancelled();
//
//            Toast.makeText(WordPuzzleActivity.this, "타이머 정지됨", Toast.LENGTH_SHORT).show();
//
//
//        }
//    }


    public String getPuzzleRecord(){

        return tvPuzzleTime.getText().toString();
    }

}
