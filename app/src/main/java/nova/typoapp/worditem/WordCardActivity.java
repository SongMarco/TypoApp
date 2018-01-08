package nova.typoapp.worditem;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.R;
import nova.typoapp.worditem.WordItemContent.WordItem;



/*
단어 카드 액티비티

사용자가 단어장에서 단어 카드를 클릭하면 오는 액티비티.

단어카드를 보며 영단어를 학습할 수 있다.

 */
public class WordCardActivity extends AppCompatActivity {

    private static final String TAG ="WordCardActivity" ;




    @BindView(R.id.rvWordCard)
    RecyclerView rvWordCard;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_card);
        ButterKnife.bind(this);


        // 액티비티를 건너올 때 받은 아이템 리스트를 받아 저장한다.
        ArrayList<WordItem> gotItems = new ArrayList<>();
        gotItems = getIntent().getParcelableArrayListExtra("bundleItems");


        //상단 툴바를 세팅한다.
        Toolbar toolbarComment = (Toolbar) findViewById(R.id.toolbarWordCard);

        setSupportActionBar(toolbarComment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("낱말 카드");

        MyWordCardAdapter wordCardAdapter = new MyWordCardAdapter(gotItems);

        rvWordCard.setLayoutManager( new LinearLayoutManager(this ));

        rvWordCard.setAdapter(wordCardAdapter);





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
}
