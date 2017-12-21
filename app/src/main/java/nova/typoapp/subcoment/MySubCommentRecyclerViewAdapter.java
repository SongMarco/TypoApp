package nova.typoapp.subcoment;

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
import nova.typoapp.R;
import nova.typoapp.SubCommentFragment.OnListFragmentInteractionListener;
import nova.typoapp.subcoment.SubCommentContent.SubCommentItem;


/*
대댓글 리사이클러뷰의 어댑터다.

뷰홀더에서 필요한 기본 뷰들을 세팅한다.

온바인드 뷰홀더에서 대댓글 아이템의 데이터(내용, 작성자, 작성일 등)를
대댓글 뷰에 세팅하게 된다.

 */


/**
 * {@link RecyclerView.Adapter} that can display a {@link SubCommentItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */

public class MySubCommentRecyclerViewAdapter extends RecyclerView.Adapter<MySubCommentRecyclerViewAdapter.ViewHolder> {

    private final List<SubCommentItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MySubCommentRecyclerViewAdapter(List<SubCommentItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_subcoment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        SubCommentItem item = mValues.get(position);

        //더미데이터들. 필요시 삭제 가능
        holder.mItem = mValues.get(position);

        // 대댓글 작성자, 내용, 날짜 데이터 세팅
        if(item.subCommentWriter!=null){
            holder.mSubCommentWriterView.setText(item.subCommentWriter);
            holder.mSubCommentContentView.setText(item.subCommentContent);
            holder.mSubCommentDateView.setText(item.subCommentDate);
        }


        // 프로필 이미지의 유무에 따라 프로필 이미지뷰 세팅. 없으면 -> 기본 세팅
        if (mValues.get(position).subCommentimgProfileUrl != null && !mValues.get(position).subCommentimgProfileUrl.equals("") ) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(holder.mView).load(mValues.get(position).subCommentimgProfileUrl)
                    .apply(requestOptions)
                    .into(holder.mSubCommentProfileImageView);
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


    /*
    아이템의 뷰 형태를 세팅하는 뷰홀더.
    onBindViewHolder 에서 아이템의 데이터를 세팅한다.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;


        public SubCommentItem mItem;


        @BindView(R.id.subCommentProfileImage)
        ImageView mSubCommentProfileImageView;

        @BindView(R.id.subCommentWriter)
        TextView mSubCommentWriterView;

        @BindView(R.id.subCommentContent)
        TextView mSubCommentContentView;

        @BindView(R.id.subCommentDate)
        TextView mSubCommentDateView;

        @BindView(R.id.subCommentLike)
        TextView mSubCommentLikeView;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            mView = view;

            //프로필 이미지를 동그랗게 하기 위한 코드.
            mSubCommentProfileImageView.setBackground(new ShapeDrawable(new OvalShape()));
            mSubCommentProfileImageView.setClipToOutline(true);

        }

    }
}
