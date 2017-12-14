package nova.typoapp.comment;

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
import nova.typoapp.CommentFragment.OnListFragmentInteractionListener;
import nova.typoapp.R;
import nova.typoapp.comment.CommentContent.CommentItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link CommentItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyCommentRecyclerViewAdapter extends RecyclerView.Adapter<MyCommentRecyclerViewAdapter.ViewHolder> {

    private final List<CommentItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyCommentRecyclerViewAdapter(List<CommentItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        CommentItem mItem = holder.mItem;

        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).content);

        holder.mCommentContentView.setText(mItem.commentContent);

        holder.mCommentWriterView.setText(mItem.commentWriter);
        holder.mCommentDateView.setText(mItem.commentDate);

        Log.e("onBindTag", "onBindViewHolder: writer= "+mItem.commentWriter+"content = "+mItem.commentContent);



// 프로필 이미지의 유무에 따라 이미지뷰 세팅. 없으면 -> 기본 세팅
        if (mValues.get(position).imgProfileUrl != null && !mValues.get(position).imgProfileUrl.equals("") ) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(holder.mView).load(mValues.get(position).imgProfileUrl)
                    .apply(requestOptions)
                    .into(holder.mCommentProfileImage);
        }

//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (null != mListener) {
//                    // Notify the active callbacks interface (the activity, if the
//                    // fragment is attached to one) that an item has been selected.
//                    mListener.onListFragmentInteraction(holder.mItem);
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        @BindView(R.id.id)
        public TextView mIdView;

        @BindView(R.id.content)
        TextView mContentView;
        @BindView(R.id.commentWriter)
        TextView mCommentWriterView;
        @BindView(R.id.commentContent)
        TextView mCommentContentView;
        @BindView(R.id.commentDate)
        TextView mCommentDateView;
        @BindView(R.id.commentProfileImage)
        ImageView mCommentProfileImage;




        public CommentItem mItem;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            mView = view;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
