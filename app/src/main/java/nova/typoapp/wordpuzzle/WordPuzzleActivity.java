package nova.typoapp.wordpuzzle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

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





public class WordPuzzleActivity extends AppCompatActivity {


    @BindView(R.id.rvWordPuzzle)
    RecyclerView rvWordPuzzle;


    public static int countPickedCards = 0;

    public static ArrayList<WordItem> listPickedItem= new ArrayList<>();
    public static ArrayList<View> listPickedView = new ArrayList<>();

    public static ArrayList<MyWordPuzzleAdapter.ViewHolder> listPickedViewHolder = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_puzzle);

        ButterKnife.bind(this);



        //상단 툴바를 세팅한다.
        Toolbar toolbarComment = (Toolbar) findViewById(R.id.toolbarWordPuzzle);

        setSupportActionBar(toolbarComment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("단어 퍼즐");


        // 액티비티를 건너올 때 받은 아이템 리스트를 받아 저장한다.
        ArrayList<WordItem> gotItems = new ArrayList<>();
        gotItems = getIntent().getParcelableArrayListExtra("bundleItems");

        // gotItems 의 사이즈가 9보다 크면, 9개가 되도록 줄여야 한다.(화면이 넘쳐버림)


        /*

        줄이는 방법을 랜덤하게 하기 위해 셔플을 먼저 해준다.
        gotItems
                 */

        if(gotItems.size() > 9){
            Collections.shuffle(gotItems);

            gotItems = new ArrayList<WordItem>(gotItems.subList(0,9));

        }







//        for(int i = 0; i < gotItems.size(); i ++){
//
//            gotItems.get(i).getItemInfo();
//
//        }

        ArrayList<WordItem> gotItemsCopy = new ArrayList<>();

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


        MyWordPuzzleAdapter puzzleAdapter = new MyWordPuzzleAdapter(gotItemsCopy);

        GridLayoutManager mLayoutManager = new GridLayoutManager(this,3);

        rvWordPuzzle.setLayoutManager(mLayoutManager);
        rvWordPuzzle.setAdapter(puzzleAdapter);




        // 이미지 리스트와 뜻 리스트를 만든다.


        //단어 뜻과 이미지를 분리한다.




        //단어 리스트에 대한 리사이클러뷰를 세팅한다.





    }



    //화면을 벗어나면 스태틱 변수들을 초기화한다.

    @Override
    protected void onStop() {
        super.onStop();


        WordPuzzleActivity.countPickedCards = 0;
        WordPuzzleActivity.listPickedItem.clear();
        WordPuzzleActivity.listPickedView.clear();
        WordPuzzleActivity.listPickedViewHolder.clear();

    }
}
