package nova.typoapp.worditem;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.R;
import nova.typoapp.worditem.WordItemContent.WordItem;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Administrator on 2018-01-04.
 */

public class MyWordCardAdapter extends RecyclerView.Adapter<MyWordCardAdapter.ViewHolder> {

    private final List<WordItem> mValues;


    public MyWordCardAdapter(List<WordItem> items) {
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
    public MyWordCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_word_card_item, parent, false);
        return new MyWordCardAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final MyWordCardAdapter.ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        final WordItem item = holder.mItem;

        holder.tvCardWordName.setText( item.nameWord );
        holder.tvCardWordMean.setText( item.meanWord );





        // 프로필 이미지의 유무에 따라 프로필 이미지뷰 세팅. 없으면 -> 기본 세팅

        Log.e("abc", "onBindViewHolder: "+item.UrlWordImg );
        if (item.UrlWordImg != null && !item.UrlWordImg.equals("")) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(holder.mView).load(item.UrlWordImg)
                    .apply(requestOptions)
                    .into(holder.imgWordCard);
        }



        View.OnClickListener onWordClickListener = new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                //댓글 달기 버튼 혹은 댓글 갯수를 클릭했다면 댓글 액티비티로 이동시킨다.

                int clickedViewId = v.getId();

                if (clickedViewId == holder.imgSoundCard.getId() ){

                    //발음 관련 변수들 초기화


//                        Toast.makeText(context, "speak clicked "+item.getTitle(), Toast.LENGTH_SHORT).show();

                    String text = item.nameWord;
//                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();

                    //발음 실행. 주의사항 : 롤리팝 버전 이상에서만 가능. 추가 코드 필요

                    String utteranceId = this.hashCode() + "";
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                }


                //카드를 눌렀다. 카드를 뒤집어 뜻을 보여주도록 한다.
                if (clickedViewId == holder.mView.getId() ){

//                    Toast.makeText(v.getContext(), "카드 뒤집기", Toast.LENGTH_SHORT).show();

                    View card = holder.mView;
                    final ObjectAnimator oa1 = ObjectAnimator.ofFloat(holder.mView, "scaleX", 1f, 0f);
                    final ObjectAnimator oa2 = ObjectAnimator.ofFloat(holder.mView, "scaleX", 0f, 1f);
//                    oa1.setInterpolator(new DecelerateInterpolator());
//                    oa2.setInterpolator(new AccelerateDecelerateInterpolator());

                    oa1.setInterpolator(new DecelerateInterpolator());
                    oa2.setInterpolator(new AccelerateDecelerateInterpolator());

                    oa1.setDuration(200);
                    oa2.setDuration(200);

                    oa1.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
//                            imageView.setImageResource(R.drawable.frontSide);

                            if( holder.frameForwardCard.getVisibility()==View.INVISIBLE ){

                                holder.frameForwardCard.setVisibility(View.VISIBLE);
                                holder.frameBackwardCard.setVisibility(View.INVISIBLE);
                            }
                            else{
                                holder.frameForwardCard.setVisibility(View.INVISIBLE);
                                holder.frameBackwardCard.setVisibility(View.VISIBLE);
                            }

                            oa2.start();
                        }
                    });
                    oa1.start();
                }



            }




        };

        holder.mView.setOnClickListener(onWordClickListener);
        holder.imgSoundCard.setOnClickListener(onWordClickListener);







    }
    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;

        public WordItem mItem;

        @BindView(R.id.imgWordCard)
        ImageView imgWordCard;

        @BindView(R.id.imgSoundCard)
        ImageView imgSoundCard;

        @BindView(R.id.frameForwardCard)
        FrameLayout frameForwardCard;

        ////////뒷면 내용
        @BindView(R.id.frameBackward)
        FrameLayout frameBackwardCard;

        @BindView(R.id.tvCardWordName)
        TextView tvCardWordName;

        @BindView(R.id.tvCardWordMean)
        TextView tvCardWordMean;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;

            Glide.with(view.getContext()).load(R.drawable.ic_volume_up_black_24dp).into(imgSoundCard);

        }

    }


}
