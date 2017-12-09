package nova.typoapp.newsfeed;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import nova.typoapp.NewsFeedFragment.OnListFragmentInteractionListener;
import nova.typoapp.R;
import nova.typoapp.WriteActivity;
import nova.typoapp.newsfeed.NewsFeedContent.FeedItem;
import nova.typoapp.retrofit.ApiService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.newsfeed.NewsFeedContent.ITEMS;
import static nova.typoapp.retrofit.ApiService.API_URL;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FeedItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyNewsFeedRecyclerViewAdapter extends RecyclerView.Adapter<MyNewsFeedRecyclerViewAdapter.ViewHolder> {

    private final List<FeedItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final ClickListener listener;

    public MyNewsFeedRecyclerViewAdapter(List<FeedItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        this.listener = null;
    }

    public MyNewsFeedRecyclerViewAdapter(List<FeedItem> items, ClickListener clickListener) {
        mValues = items;
        this.listener = clickListener;

        mListener = null;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_newsfeed_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        holder.mWriterView.setText("" + mValues.get(position).writer);
        holder.mIdView.setText("단어 : " + mValues.get(position).title);
        holder.mContentView.setText("뜻 : " + mValues.get(position).content);

        if (!Objects.equals(mValues.get(position).imgUrl, "")) {

//            Log.e("imgUrl", "onBindViewHolder: "+mValues.get(position).imgUrl );

            Glide.with(holder.mView).load(mValues.get(position).imgUrl).into(holder.mImageView);
        } else {
            Glide.with(holder.mView).load(R.drawable.ic_launcher_round).into(holder.mImageView);
        }

        if (!mValues.get(position).imgProfileUrl.equals("")) {
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

    public interface ClickListener {

        void onPositionClicked(int position);

        void onLongClicked(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
            mWriterView = (TextView) view.findViewById(R.id.feedWriter);
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);

            mImageView = (ImageView) view.findViewById(R.id.imageViewItem);
            mProfileView = (ImageView) view.findViewById(R.id.imageProf);
            mMoreView = (ImageView) view.findViewById(R.id.imageViewMore);

            mProfileView.setBackground(new ShapeDrawable(new OvalShape()));
            mProfileView.setClipToOutline(true);


            mMoreView.setOnClickListener(this);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        @Override
        public void onClick(View v) {
            final Context context = v.getContext();
            if (v.getId() == mMoreView.getId()) {


                new AlertDialog.Builder(context)

//                .setTitle("업로드 방식 선택")
                        .setItems(R.array.edit_del, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item

                                switch (which) {
                                    case 0:
                                        FeedItem item = ITEMS.get(getAdapterPosition());

                                        Intent intent = new Intent(context, WriteActivity.class);
                                        intent.putExtra("imgUrl", item.getImgUrl());
                                        intent.putExtra("title", item.getTitle());
                                        intent.putExtra("content", item.getContent());
                                        intent.putExtra("feedID", item.getFeedID());
                                        context.startActivity(intent);



//                                        Toast.makeText(context, "수정 클릭 = " + String.valueOf(ITEMS.get(getAdapterPosition()).getInfo()), Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:





                                        AlertDialog.Builder builder =  new AlertDialog.Builder(context)
                                                .setMessage("정말 삭제하시겠습니까?")
                                                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        FeedItem item = ITEMS.get(getAdapterPosition());
                                                        DeleteTask deleteTask = new DeleteTask();
                                                        deleteTask.execute(item.getFeedID());
                                                    }
                                                })
                                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });


                                        AlertDialog dialog2 = builder.create();
                                        dialog2.show();







//                                        ITEMS.remove(item);
//



//                                        Toast.makeText(context, "삭제 클릭 = " + String.valueOf(ITEMS.get(getAdapterPosition()).getInfo()), Toast.LENGTH_SHORT).show();


                                        break;

                                }
                            }
                        })
                        .show();
            } else {
                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }


        }
    }

    String json_result = "";

    public class DeleteTask extends AsyncTask<Integer, String, String> {


        @Override
        protected String doInBackground(Integer... integers) {

            //region//글 삭제하기

            Retrofit retrofit = new Retrofit.Builder().baseUrl(API_URL).build();
            ApiService apiService = retrofit.create(ApiService.class);

            Call<ResponseBody> comment = apiService.deleteFeed(integers[0]);


            try {

                json_result = comment.execute().body().string();
                return json_result;
            } catch (IOException e) {
                e.printStackTrace();
            }




            return null;
        }

        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            Log.e("wow", result);


        }

    }

}



