package nova.typoapp.newsfeed;

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
import com.bumptech.glide.signature.ObjectKey;

import java.util.List;
import java.util.Objects;

import nova.typoapp.NewsFeedFragment.OnListFragmentInteractionListener;
import nova.typoapp.R;
import nova.typoapp.newsfeed.NewsFeedContent.FeedItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FeedItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyNewsFeedRecyclerViewAdapter extends RecyclerView.Adapter<MyNewsFeedRecyclerViewAdapter.ViewHolder> {

    private final List<FeedItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyNewsFeedRecyclerViewAdapter(List<FeedItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_newsfeed_item, parent, false);
        return new ViewHolder(view);
    }

    public interface OnRecyclerItemClickListener {

        void onRecyclerItemClick(String data);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        holder.mWriterView.setText( ""+mValues.get(position).writer);
        holder.mIdView.setText("단어 : "+mValues.get(position).title);
        holder.mContentView.setText("뜻 : "+ mValues.get(position).content);

        if(!Objects.equals(mValues.get(position).imgUrl, "")){

//            Log.e("imgUrl", "onBindViewHolder: "+mValues.get(position).imgUrl );

            Glide.with(holder.mView).load(mValues.get(position).imgUrl ).into(holder.mImageView);
        }
        else{
            Glide.with(holder.mView).load(R.drawable.ic_launcher_round).into(holder.mImageView);
        }

        if( !mValues.get(position).imgProfileUrl.equals("") ){
            RequestOptions requestOptions = new RequestOptions()
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));

            Glide.with(holder.mView).load(mValues.get(position).imgProfileUrl)
                    .apply(requestOptions)
                    .into(holder.mProfileView);
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
        public TextView mWriterView;
        public final TextView mIdView;
        public final TextView mContentView;
        public FeedItem mItem;


        public ImageView mImageView;

        public ImageView mProfileView;

        public ImageView mMoreView;



        public ViewHolder(View view) {
            super(view);
            mView = view;
            mWriterView = (TextView)view.findViewById(R.id.feedWriter);
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);

            mImageView = (ImageView) view.findViewById(R.id.imageViewItem);
            mProfileView = (ImageView) view.findViewById(R.id.imageProf);
            mMoreView = (ImageView)view.findViewById(R.id.imageViewMore);

            mProfileView.setBackground(new ShapeDrawable(new OvalShape()));
            mProfileView.setClipToOutline(true);


        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
