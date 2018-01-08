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

    단어장 액티비티에서 넘어오는 화면으로써,
    단어 매칭 게임을 플레이할 수 있다.

    2. 구성

        2-1) 게임 준비 프래그먼트(버튼을 눌러 게임을 시작할 수 있음)

        2-2) 매칭 게임 프래그먼트(게임을 진행)
            2-2-a) 단어 아이템을 담아둔 리사이클러뷰(그리드 형식)

        2-3) 게임 완료 프래그먼트(버튼을 눌러 게임을 다시 시작하거나 뒤로가기를 눌러 종료)


    3. 동작

        3-1) 퍼즐 초기화 하기

        단어장 액티비티에서 넘어올 때
        단어 아이템들이 담긴 어레이 리스트를 인텐트에서 받아 변수로 저장한다.
        - 리스트를 그대로 쓰지 않는 이유는, 추가로 리스트 정보를 가공해야 하기 때문이다.

        단어 리스트에 어댑터와 리사이클러뷰를 세팅하여
        퍼즐을 초기화한다.

        3-2) 퍼즐 풀기

        퍼즐의 리사이클러뷰(이하 rv)에는 단어 아이템들이 세팅돼있다.

        단어 아이템 두 개를 클릭하면 정답을 확인한다.

        정답이면 두 카드가 사라진다.(애니메이션)

        오답이면 카드가 흔들린다.


    4. 특이사항

 */


public class WordPuzzleActivity extends AppCompatActivity

        implements WordPuzzleStartFragment.OnFragmentInteractionListener,
        WordPuzzlePlayFragment.OnFragmentInteractionListener,
        WordPuzzleEndFragment.OnFragmentInteractionListener {


    // 플레이어가 집은 카드의 갯수 - 2개가 될 때 정답 여부를 확인한다.
    public static int countPickedCards = 0;

    // 플레이어가 맞춘 단어 아이템의 갯수. 맞춘 갯수가 전체 단어 아이템 수와 같으면 게임을 클리어한다.
    public static int countCorrectItems = 0;

    //플레이어가 집은 카드의 어레이 리스트
    public static ArrayList<WordItem> listPickedItem = new ArrayList<>();


    // 플레이어가 집은 카드의 뷰홀더.(단어 아이템 뷰의 상태를 변화시킬 때 사용 ex) 카드 색깔 변화, 애니메이션 등)
    public static ArrayList<MyWordPuzzleAdapter.ViewHolder> listPickedViewHolder = new ArrayList<>();


    @BindView(R.id.containerFragmentPuzzle)
    FrameLayout containerFragmentPuzzle;


    //퍼즐의 타이머를 위한 텍스트뷰. - 처음에는 보이지 않다가, 게임을 시작하면 보이게 된다.
    @BindView(R.id.tvPuzzleTime)
    TextView tvPuzzleTime;



    //단어 아이템의 어레이 리스트를 복사한 어레이 리스트.(단어 아이템들의 데이터의 가공을 위해 사용 - 랜덤하게 섞기 등등)
    public static ArrayList<WordItem> gotItemsCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_puzzle);

        ButterKnife.bind(this);

        //상단 툴바를 세팅한다.
        Toolbar toolbarComment = (Toolbar) findViewById(R.id.toolbarWordPuzzle);
        setSupportActionBar(toolbarComment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("단어 매칭 게임");


        //게임 시작 프래그먼트를 세팅한다.
        //이 프래그먼트에는 게임 시작 버튼과 게임에 대한 간단한 설명을 볼 수 있다.
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

        //게임 시작 프래그먼트 객체를 생성
        WordPuzzleStartFragment startFragment = new WordPuzzleStartFragment();
        Bundle bundle = new Bundle();
        startFragment.setArguments(bundle);

        //프래그맨트의 트랜잭션 수행 -> 프래그먼트가 액티비티 화면에 세팅된다.
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.containerFragmentPuzzle, startFragment); // Activity 레이아웃의 View ID
        fragmentTransaction.commit();





        // 단어장 액티비티로부터 들어온 인텐트에서 단어 아이템의 리스트를 꺼내 저장한다.
        ArrayList<WordItem> gotItems = new ArrayList<>();
        gotItems = getIntent().getParcelableArrayListExtra("bundleItems");

        /*
        gotItems 의 사이즈가 9보다 크면, 9개가 되도록 줄여야 한다.
        (퍼즐 카드가 너무 많아서 화면이 넘쳐버림, 스크롤 해야 함)



        제외할 아이템을 랜덤하게 정하기 위해 단어 리스트를 셔플하고,
        크기가 9가 되도록 subList 를 추출한다.

        랜덤하게 추출하지 않고, 셔플을 했던 이유는
        난수 생성의 중복을 생각하지 않아도 되기 때문이다.


        */

        gotItemsCopy = new ArrayList<>();

        if (gotItems.size() > 9) {
            //리스트를 셔플한다.
            Collections.shuffle(gotItems);

            //리스트를 9개까지 추출한다. 여기서 subLIst(0,9)는 인덱스 0~8까지의 9개를 의미한다.(9는 제외됨에 유의, 자바 문법)
            gotItems = new ArrayList<WordItem>(gotItems.subList(0, 9));

        }


        //gotItemsCopy 에 gotItem 을 deep Copy 한다.
        // shallow copy 를 할 경우, 데이터를 가공할 때 아이템 데이터가 같이 변경이 되기 때문에 따로 deep copy 를 진행
        for (int i = 0; i < gotItems.size(); i++) {

            gotItemsCopy.add(new WordItem(gotItems.get(i)));

        }

        //단어 이미지로 이루어진 카드와 단어 뜻으로 이루어진 카드를 각각 세팅하기 위한 조치.
        // 카피한 세트의 imgUrl은 비워둔다.
        // 이렇게 하면 퍼즐 리사이클러뷰의 어댑터에서 imgUrl 이 비었을 때 단어의 텍스트를 보여준다.
        for (int i = 0; i < gotItems.size(); i++) {

            gotItemsCopy.get(i).UrlWordImg = "";

        }

        //gotItemsCopy 에
        gotItemsCopy.addAll(gotItems);

//        Log.e("wow", "onCreate: after Shuffle ######" );


        // 아이템을 셔플한다.
        Collections.shuffle(gotItemsCopy);

        for (int i = 0; i < gotItems.size(); i++) {

            gotItemsCopy.get(i).getItemInfo();

        }


        //static 한 아이템 어레이를 만들었다. 이 변수는 액티비티에서 나가면 초기화되며,
        //액티비티에 들어오면 미리 세팅 되있다가, 퍼즐을 플레이할 때 어댑터에 세팅된다.
        // static ㅎ ㅏ므로 초기화로 인한 오류가 없는지 확인할 것.


    }


    public void setCopyList() {


    }



    //액티비티가 사라지면 스태틱 변수들을 초기화한다.
    //@@@ 초기화가 제대로 되지 않을 경우,
    // 매칭 게임을 다시 플레이할 때 단어 목록이 중첩되는 현상이 발생.
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


    //


    /*
    타이머는
    asyncTask 로는 다른 스레드(영어 발음하는 부분)와 동시동작이 안되므로
    runnable 인터페이스로 구현하였다.

    또한 Thread.sleep(time) 대신,
    기준 시간(startTime) 으로부터의 시간차를 계산하는 방식으로 시간을 측정했다.
    */


    //타이머 객체와, 측정 시작 시간 클래스 변수
    Timer stopwatchTimer;
    public long startTime = 0;

    public void startTimer() {
        stopwatchTimer = new Timer();

        //시간을 측정할 기준 시간을 currentTimeMillis 함수로 생성한다.
        startTime = System.currentTimeMillis();


        // scheduleAtFixedRate 메소드를 호출 -> 시간을 표시한 텍스트뷰를 일정 간격으로 계속 수정하는 작업을 진행
        // delay 가 0, period 가 10(1000분의 10초)이므로, 0.01초 간격으로 작업이 진행됨.
        stopwatchTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {


                    // timerTextView 는 스톱 워치의 시간을 나타내는 뷰다.
                    // 러너블 객체를 새로 만들어 현재 시간 값을 구한 후, 텍스트뷰에 세팅한다.(stopwatch() 메소드 참조)
                    @Override
                    public void run() {
                        TextView timerTextView = (TextView) findViewById(R.id.tvPuzzleTime);
                        timerTextView.setText(stopwatch());
                    }
                });

            }
        }, 0, 10);
    }

    //타이머 객체가 세팅되어 있다면, 타이머 객체 stopwatchTimer 의 작업을 정지시키고, null 로 만들어 할당된 메모리 자원을 회수한다.
    //매칭 게임이 끝났거나, 뒤로가기 키를 눌러 게임을 종료할 때 호출된다.
    public void stopTimer() {
        if (stopwatchTimer != null) {
            stopwatchTimer.cancel();
            stopwatchTimer.purge();
            stopwatchTimer = null;
        }
    }

    /*
     기준 시간으로부터 몇 초가 지났는지 측정하는 함수.
      */
    public String stopwatch() {
        //초기 시간에서 차이를 구할 현재 시간을 측정
        long nowTime = System.currentTimeMillis();

        // 시간 차이를 구하고, 시간 차이를 date 형식으로 만든다.
        long cast = nowTime - startTime;
        Date date = new Date(cast);

        // '15.3'의 형식으로 date 형식을 변환 후 함수 리턴. -> 이것이 텍스트뷰를 통해 화면에 표시됨
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("s.S");
        return simpleDateFormat.format(date);
    }



    /*
    Asynctask 로 구현한 타이머.
    이 타이머를 사용하면 영어 단어 tts 를 읽어주는 스레드가 정상적으로 동작하지 않는다.

     */
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


    // 게임 종료 화면에서 플레이어의 시간 기록을 텍스트뷰에 세팅하는 메소드
    public String getPuzzleRecord() {

        return tvPuzzleTime.getText().toString();
    }

}
