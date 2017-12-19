package nova.typoapp.comment;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
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
import nova.typoapp.CoCommentActivity;
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

//        holder.mIdView.setText(mValues.get(position).id);
//        holder.mContentView.setText(mValues.get(position).content);

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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;

//        @BindView(R.id.id)
//        public TextView mIdView;
//        @BindView(R.id.content)
//        TextView mContentView;


        // 덧글 리사이클러뷰의 내부 뷰들을 세팅한다
        @BindView(R.id.commentWriter)
        TextView mCommentWriterView;
        @BindView(R.id.commentContent)
        TextView mCommentContentView;
        @BindView(R.id.commentDate)
        TextView mCommentDateView;
        @BindView(R.id.commentProfileImage)
        ImageView mCommentProfileImage;


        @BindView(R.id.textViewCoComment)
        TextView mCoComment;

        @BindView(R.id.textViewCoCommentNum)
        TextView mCoCommentNum;


        public CommentItem mItem;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;

            //프로필 이미지를 동그랗게 하기 위한 코드.
            mCommentProfileImage.setBackground(new ShapeDrawable(new OvalShape()));
            mCommentProfileImage.setClipToOutline(true);


            //답글 보기, 답글 수에 클릭 리스너를 세팅한다.
            mCoComment.setOnClickListener(this);
            mCoCommentNum.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            final Context context = v.getContext();

            // 답글보기 혹은 답글 갯수를 클릭하면 답글 액티비티로 이동시킨다.
            if(v.getId() == mCoComment.getId() || v.getId() == mCoCommentNum.getId()   ){


                CommentItem item = CommentContent.ITEMS.get(getAdapterPosition());

                Intent intent = new Intent(v.getContext(), CoCommentActivity.class);
//                intent.putExtra("feedID", item.getFeedID());
//                NewsFeedFragment.isWentCommentActivity = true;
                v.getContext().startActivity(intent);
//                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }
            else{

            }
        }


//        @Override
//        public String toString() {
//            return super.toString() + " '" + mContentView.getText() + "'";
//        }
    }
}
