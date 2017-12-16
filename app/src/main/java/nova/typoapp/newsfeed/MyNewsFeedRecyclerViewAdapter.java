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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.CommentActivity;
import nova.typoapp.MainActivity;
import nova.typoapp.NewsFeedFragment;
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

/*
 리사이클러뷰의 어댑터다. 300줄가량 되는 복잡한 코드이니 주석을 잘 작성하도록 하자
 참고한 코드는 스택오버플로우
 https://stackoverflow.com/questions/30284067/handle-button-click-inside-a-row-in-recyclerview
 */

public class MyNewsFeedRecyclerViewAdapter extends RecyclerView.Adapter<MyNewsFeedRecyclerViewAdapter.ViewHolder> {



    // 아이템들의 리스트를 만든다.
    private final List<FeedItem> mValues;

    // 아이템 요소를 클릭할 때 반응할 클릭 리스너다.
    // 이 리스너는 바깥에서 이 리사이클러뷰를 호출할 때 사용된다.
    // 내 코드에서는 클릭한 아이템을 이 클래스에서 곧바로 활용 가능하므로,
    // 적용하지 않았다. -> 보충 필요
    private final ClickListener listener;



    /*
    리사이클러뷰 생성자들.
     */

    public MyNewsFeedRecyclerViewAdapter(List<FeedItem> items, ClickListener clickListener) {
        mValues = items;
        this.listener = clickListener;
    }


    // 뷰홀더와 프래그먼트 아이템을 매칭시키는 함수
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_newsfeed_item, parent, false);
        return new ViewHolder(view);
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


        @BindView(R.id.feedWriter)
        TextView mWriterView;
        @BindView(R.id.id)
        TextView mIdView;

        @BindView(R.id.content)
        TextView mContentView;

        @BindView(R.id.textViewDate)
        TextView mDateView;

        @BindView(R.id.imageViewItem)
        public ImageView mImageView;

        @BindView(R.id.imageProf)
        ImageView mProfileView;

        @BindView(R.id.imageViewMore)
        ImageView mMoreView;

        @BindView(R.id.layoutComment)
        RelativeLayout mLayoutComment;

        @BindView(R.id.textViewCommentNum)
        TextView mCommentNum;


        public FeedItem mItem;


        public ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);

            mView = view;


            mProfileView.setBackground(new ShapeDrawable(new OvalShape()));
            mProfileView.setClipToOutline(true);

            mMoreView.setOnClickListener(this);
            mView.setOnClickListener(this);
            mLayoutComment.setOnClickListener(this);
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

                                        //현재 컨텍스트는 메인 액티비티이다. asynctask로 컨텍스트 전달 또한 가능하다.
                                        AlertDialog.Builder builder =  new AlertDialog.Builder(context)
                                                .setMessage("정말 삭제하시겠습니까?")
                                                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        FeedItem item = ITEMS.get(getAdapterPosition());



                                                        DeleteTask deleteTask = new DeleteTask(context);
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
                FeedItem item = ITEMS.get(getAdapterPosition());
//                Intent intent = new Intent(context, WriteActivity.class);
//                intent.putExtra("imgUrl", item.getImgUrl());
//                intent.putExtra("title", item.getTitle());
//                intent.putExtra("content", item.getContent());
//                intent.putExtra("feedID", item.getFeedID());
//                context.startActivity(intent);


                Intent intent = new Intent(v.getContext(), CommentActivity.class);
                intent.putExtra("feedID", item.getFeedID());
                NewsFeedFragment.isWentCommentActivity = true;
                v.getContext().startActivity(intent);
//                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }


        }
    }


    //뷰홀더가 아이템에 달라붙을 때 어떤 일을 할지 정의하는 함수
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);


        FeedItem item = mValues.get(position);

        // 리사이클러뷰 아이템에 각 뷰를 세팅한다(작가, 단어, 뜻, 날짜 등)
        holder.mWriterView.setText("" + mValues.get(position).writer);
        holder.mIdView.setText("단어 : " + mValues.get(position).title);
        holder.mContentView.setText("뜻 : " + mValues.get(position).content);
        holder.mDateView.setText(mValues.get(position).writtenDate );


        holder.mCommentNum.setText("댓글 "+item.commentNum+"개");



        // 조건문이 나뉘는 경우 - 이미지 Url, 프로필Url은 없을 수도 있으니까.

        //이미지 Url의 유무에 따라 이미지뷰를 세팅함. 이미지 없으면 -> 기본 이미지를 세팅함
        if (!Objects.equals(mValues.get(position).imgUrl, "")) {

//            Log.e("imgUrl", "onBindViewHolder: "+mValues.get(position).imgUrl );

            Glide.with(holder.mView).load(mValues.get(position).imgUrl).into(holder.mImageView);
        } else {
            Glide.with(holder.mView).load(R.drawable.ic_launcher_round).into(holder.mImageView);
        }

        // 프로필 이미지의 유무에 따라 이미지뷰 세팅. 없으면 -> 기본 세팅
        if (!mValues.get(position).imgProfileUrl.equals("") && mValues.get(position).imgProfileUrl != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);



            Glide.with(holder.mView).load(mValues.get(position).imgProfileUrl)
                    .apply(requestOptions)

                    .into(holder.mProfileView);




        }
        else{

            Glide.with(holder.mView).load(R.drawable.com_facebook_profile_picture_blank_square).into(holder.mProfileView);
        }


    }

    String json_result = "";

    public class DeleteTask extends AsyncTask<Integer, String, String> {

        private Context mContext;

        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.

        public DeleteTask (Context context){
            mContext = context;
        }

        @Override
        protected String doInBackground(Integer... integers) {

            //region//글 삭제하기 - DB상에서 뉴스피드 글을 삭제한다.

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

            //삭제가 완료되었다. 여기서 새로고침을 진행하자
            MainActivity activity = (MainActivity)mContext;
            activity.getPagerAdapter().notifyDataSetChanged();



            Log.e("wow", result);


        }

    }

}



