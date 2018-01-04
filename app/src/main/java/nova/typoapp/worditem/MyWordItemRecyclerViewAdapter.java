package nova.typoapp.worditem;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * Created by Administrator on 2018-01-03.
 */

public class MyWordItemRecyclerViewAdapter extends RecyclerView.Adapter<MyWordItemRecyclerViewAdapter.ViewHolder> {

    private final List<WordItem> mValues;


    public MyWordItemRecyclerViewAdapter(List<WordItem> items) {
        mValues = items;

    }



    TextToSpeech tts =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if(status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.ENGLISH);
            }
        }
    });


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_word_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        final WordItem item = holder.mItem;

        holder.tvNameWord.setText(item.nameWord);

        holder.tvMeanWord.setText(item.meanWord);


        // 프로필 이미지의 유무에 따라 프로필 이미지뷰 세팅. 없으면 -> 기본 세팅


        Log.e("abc", "onBindViewHolder: "+item.UrlWordImg );
        if (item.UrlWordImg != null && !item.UrlWordImg.equals("")) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(holder.mView).load(item.UrlWordImg)
                    .apply(requestOptions)
                    .into(holder.imgWord);
        }



        View.OnClickListener onWordClickListener = new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                //댓글 달기 버튼 혹은 댓글 갯수를 클릭했다면 댓글 액티비티로 이동시킨다.

                int clickedViewId = v.getId();

                if (clickedViewId == holder.imgSoundWord.getId() ){

                    //발음 관련 변수들 초기화


//                        Toast.makeText(context, "speak clicked "+item.getTitle(), Toast.LENGTH_SHORT).show();

                    String text = item.nameWord;
//                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();


                    //발음 실행. 주의사항 : 롤리팝 버전 이상에서만 가능. 추가 코드 필요

                    String utteranceId = this.hashCode() + "";
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                }
            }
        };

        holder.imgSoundWord.setOnClickListener(onWordClickListener);








//        //아이템에 클릭 리스너 세팅
//
//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//            }
//        });
    }
    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public WordItem mItem;


        @BindView(R.id.tvNameWord)
        TextView tvNameWord;

        @BindView(R.id.tvMeanWord)
        TextView tvMeanWord;

        @BindView(R.id.imgWord)
        ImageView imgWord;

        @BindView(R.id.imgSoundWord)
        ImageView imgSoundWord;




        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;

            Glide.with(view.getContext()).load(R.drawable.ic_volume_up_black_24dp).into(imgSoundWord);


        }

    }
}
