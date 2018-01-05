package nova.typoapp.wordpuzzle;

import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.R;
import nova.typoapp.worditem.WordItemContent.WordItem;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Administrator on 2018-01-05.
 */

public class MyWordPuzzleAdapter extends RecyclerView.Adapter<MyWordPuzzleAdapter.ViewHolder> {

    private final List<WordItem> mValues;


    public MyWordPuzzleAdapter(List<WordItem> items) {
        mValues = items;

    }



    private TextToSpeech tts =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if(status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.ENGLISH);
            }
        }
    });


    @Override
    public MyWordPuzzleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_puzzle_item, parent, false);
        return new MyWordPuzzleAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final MyWordPuzzleAdapter.ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        final WordItem item = holder.mItem;

        holder.tvPuzzleWord.setText(item.meanWord);


        //퍼즐의 이미지와 뜻을 세팅하는 코드
        if (item.UrlWordImg != null && !item.UrlWordImg.equals("")) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            holder.imgPuzzle.setVisibility(View.VISIBLE);
            holder.layoutPuzzleText.setVisibility(View.INVISIBLE);

            Glide.with(holder.mView).load(item.UrlWordImg)
                    .apply(requestOptions)
                    .into(holder.imgPuzzle);
        }
        else{
            holder.imgPuzzle.setVisibility(View.INVISIBLE);
            holder.layoutPuzzleText.setVisibility(View.VISIBLE);

        }


        View.OnClickListener mPuzzleClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //카드를 클릭했는데, 선택되지 않은 카드다.

                if(view.getBackground()==null  ){

                    //카드를 파랗게 칠하고,
                    //픽된 카드 수를 증가시켜, 2가 될 경우 정답을 검증하고 클리어.
                    view.setBackgroundColor(view.getContext().getResources().getColor(R.color.transBlue));

                    WordPuzzleActivity.countPickedCards++;
                    WordPuzzleActivity.listPickedItem.add(item);
                    WordPuzzleActivity.listPickedViewHolder.add(holder);

                    /*
                    픽한 카드가 2개가 되면 정답을 검증하고,
                    픽한 카드를 새었던 변수들을 초기화한다.
                     */
                    if( WordPuzzleActivity.countPickedCards == 2){


                        //선택한 아이템의 뜻을 비교해서 맞으면 정답, 틀리면 오답이다.
                        if(Objects.equals(WordPuzzleActivity.listPickedItem.get(0).meanWord, WordPuzzleActivity.listPickedItem.get(1).meanWord)){

                            //답을 맞췄다. 두 아이템의 뷰를 안보이게 한다.
//                            Toast.makeText(view.getContext(), "정답!", Toast.LENGTH_SHORT).show();

                            for(int i = 0; i < 2; i++){

                                Animation fade_out = AnimationUtils.loadAnimation(view.getContext(), R.anim.card_fade_out);

                                WordPuzzleActivity.listPickedViewHolder.get(i).mView.startAnimation(fade_out);
                                WordPuzzleActivity.listPickedViewHolder.get(i).mView.setVisibility(View.INVISIBLE);

                            }


                            //답을 틀렸다.
                        }
                        else{
                            Toast.makeText(view.getContext(), "오답!", Toast.LENGTH_SHORT).show();
                        }



                        //아래 코드에 의해, 선택한 아이템이 초기화되고,
                        //색칠된 뷰가 원상복구된다. - 비동기 태스크에서 사용할 것
                        for(int i = 0; i < 2; i++){

                            WordPuzzleActivity.listPickedViewHolder.get(i).framePuzzle.setBackground(null);

                        }
                        WordPuzzleActivity.countPickedCards = 0;
                        WordPuzzleActivity.listPickedItem.clear();
                        WordPuzzleActivity.listPickedViewHolder.clear();


                    }


                    Log.e("countPickedCards", "onClick: picked ="+WordPuzzleActivity.countPickedCards );
                }
                //아래 상황은, 선택한 아이템을 다시 클릭했을 대 발생한다.
                // 스태틱 변수들을 초기화해주고, 색칠한 것을 원상복구한다.
                else{

                    view.setBackground(null);

                    WordPuzzleActivity.countPickedCards = 0;
                    WordPuzzleActivity.listPickedItem.clear();
                    WordPuzzleActivity.listPickedViewHolder.clear();

                    Log.e("countPickedCards", "onClick: picked ="+WordPuzzleActivity.countPickedCards );
                }



            }
        };


        // 퍼즐의 카드에 클릭 리스너 세팅

        holder.framePuzzle.setOnClickListener(mPuzzleClickListener);













    }
    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;

        public WordItem mItem;

        @BindView(R.id.layoutPuzzleText)
        LinearLayout layoutPuzzleText;


        @BindView(R.id.tvPuzzleWord)
        TextView tvPuzzleWord;

        @BindView(R.id.imgPuzzle)
        ImageView imgPuzzle;

        @BindView(R.id.framePuzzle)
        FrameLayout framePuzzle;




        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;


        }

    }




}