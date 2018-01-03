package nova.typoapp.worditem;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.R;
import nova.typoapp.worditem.WordItemContent.WordItem;

/**
 * Created by Administrator on 2018-01-03.
 */

public class MyWordItemRecyclerViewAdapter extends RecyclerView.Adapter<MyWordItemRecyclerViewAdapter.ViewHolder> {

    private final List<WordItem> mValues;


    public MyWordItemRecyclerViewAdapter(List<WordItem> items) {
        mValues = items;

    }


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


        //아이템에 클릭 리스너 세팅

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
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
