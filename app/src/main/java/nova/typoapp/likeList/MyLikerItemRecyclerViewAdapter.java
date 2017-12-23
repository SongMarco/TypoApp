package nova.typoapp.likeList;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v7.widget.RecyclerView;
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
import nova.typoapp.LikerItemFragment.OnListFragmentInteractionListener;
import nova.typoapp.R;
import nova.typoapp.likeList.LikerContent.LikerItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link LikerItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyLikerItemRecyclerViewAdapter extends RecyclerView.Adapter<MyLikerItemRecyclerViewAdapter.ViewHolder> {

    private final List<LikerItem> mValues;
    private  OnListFragmentInteractionListener mListener;

    public MyLikerItemRecyclerViewAdapter(List<LikerItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    public MyLikerItemRecyclerViewAdapter(List<LikerItem> items) {
        mValues = items;

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_likeritem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {



        holder.mItem = mValues.get(position);
        LikerItem item = holder.mItem;

        holder.textViewLiker.setText(item.likerName);


        // 프로필 이미지의 유무에 따라 프로필 이미지뷰 세팅. 없으면 -> 기본 세팅
        if (item.likerProfileUrl != null && !(item.likerProfileUrl).equals("") ) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(holder.mView).load(item.likerProfileUrl)
                    .apply(requestOptions)
                    .into(holder.likerProfileImg);
        }


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;


        @BindView(R.id.textViewLiker)
        TextView textViewLiker;

        @BindView(R.id.likerProfileImg)
        ImageView likerProfileImg;



        public LikerItem mItem;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;

            likerProfileImg.setBackground(new ShapeDrawable(new OvalShape()));
            likerProfileImg.setClipToOutline(true);

        }


    }
}
