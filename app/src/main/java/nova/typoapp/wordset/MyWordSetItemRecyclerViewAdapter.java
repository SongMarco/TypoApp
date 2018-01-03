package nova.typoapp.wordset;

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
import nova.typoapp.wordset.WordSetContent.WordSetItem;
import nova.typoapp.wordset.WordSetFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link WordSetItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyWordSetItemRecyclerViewAdapter extends RecyclerView.Adapter<MyWordSetItemRecyclerViewAdapter.ViewHolder> {

    private final List<WordSetItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyWordSetItemRecyclerViewAdapter(List<WordSetItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_wordset_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
//        holder.mIdView.setText(mValues.get(position).id);
//        holder.mContentView.setText(mValues.get(position).content);


        WordSetItem item = holder.mItem;

        holder.tvTitleSet.setText(item.titleWordSet);

        holder.tvNameSetOwner.setText(item.nameWordSetOwner);

        holder.tvNumWords.setText( "단어 수 "+item.numWords);

        // 프로필 이미지의 유무에 따라 프로필 이미지뷰 세팅. 없으면 -> 기본 세팅


        if (item.UrlOwnerProfileImg != null && ! item.UrlOwnerProfileImg.equals("") ) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(holder.mView).load(item.UrlOwnerProfileImg)
                    .apply(requestOptions)
                    .into(holder.imgSetOwnerProfile);
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
        public  View mView;
        public  TextView mIdView;
        public  TextView mContentView;
        public WordSetItem mItem;


        @BindView(R.id.tvNameSetOwner)
        TextView tvNameSetOwner;

        @BindView(R.id.tvNumWords)
        TextView tvNumWords;

        @BindView(R.id.imgSetOwnerProfile)
        ImageView imgSetOwnerProfile;

        @BindView(R.id.tvTitleSet)
        TextView tvTitleSet;



        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
            mView = view;

            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);


            imgSetOwnerProfile.setBackground(new ShapeDrawable(new OvalShape()));
            imgSetOwnerProfile.setClipToOutline(true);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
