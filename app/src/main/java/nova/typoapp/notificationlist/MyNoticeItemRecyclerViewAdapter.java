package nova.typoapp.notificationlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.CommentActivity;
import nova.typoapp.MainActivity;
import nova.typoapp.R;
import nova.typoapp.SubCommentActivity;
import nova.typoapp.notificationlist.NoticeContent.NoticeItem;
import nova.typoapp.notificationlist.NoticeItemFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link NoticeItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyNoticeItemRecyclerViewAdapter extends RecyclerView.Adapter<MyNoticeItemRecyclerViewAdapter.ViewHolder> {

    private final List<NoticeItem> mValues;
    private OnListFragmentInteractionListener mListener;

    public MyNoticeItemRecyclerViewAdapter(List<NoticeItem> mValues) {


        this.mValues = mValues;
    }

    public MyNoticeItemRecyclerViewAdapter(List<NoticeItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_notice_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {


        holder.mItem = mValues.get(position);
        final NoticeItem item = holder.mItem;

        holder.mNoticeContent.setText(item.noticeContent);
        holder.mNoticeDate.setText(item.noticeDate);


        // 프로필 이미지의 유무에 따라 프로필 이미지뷰 세팅. 없으면 -> 기본 세팅
        if (item.profileUrl != null && ! item.profileUrl.equals("") ) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(holder.mView).load(item.profileUrl)
                    .apply(requestOptions)
                    .into(holder.mNoticeProfileImage);
        }

        View.OnClickListener mNoticeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent;
                Context context = holder.mView.getContext();
                switch (item.toActivity){

                    case "MainActivity":

                        intent = new Intent(context, MainActivity.class);

                        intent.putExtra("feedIDFromFcm", item.feedID);

                        context.startActivity(intent);

                        break;


                    case "CommentActivity":

                        intent = new Intent(context, CommentActivity.class);

                        intent.putExtra("feedIDFromFcm", item.feedID);

                        context.startActivity(intent);
                        break;


                    case "SubCommentActivity":

                        intent = new Intent(context, SubCommentActivity.class);

                        intent.putExtra("feedIDFromFcm", item.feedID);
                        intent.putExtra("commentIDFromFcm", item.commentID);

                        context.startActivity(intent);

                        break;

                }



            }
        };

        holder.mView.setOnClickListener(mNoticeClickListener);



    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }




    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public NoticeItem mItem;


        @BindView(R.id.noticeContent)
        TextView mNoticeContent;

        @BindView(R.id.noticeDate)
        TextView mNoticeDate;

        @BindView(R.id.noticeProfileImage)
        ImageView mNoticeProfileImage;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);


            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);

            mNoticeProfileImage.setBackground(new ShapeDrawable(new OvalShape()));
            mNoticeProfileImage.setClipToOutline(true);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
