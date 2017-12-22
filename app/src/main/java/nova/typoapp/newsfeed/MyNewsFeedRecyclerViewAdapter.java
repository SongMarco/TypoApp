package nova.typoapp.newsfeed;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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

public class MyNewsFeedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

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


    //왜 1을 더하지? 헤더가 있으니까!
    @Override
    public int getItemCount() {
        return mValues.size() + 1;
    }

    //헤더자리인지 검사 후 헤더자리면 헤더로 타입을 주고
    // 아니라면 일반 아이템으로 타입을 준다.
    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    //헤더인지 검사하는 함수 - position == 0인지 검사
    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    //아이템을 건져올 때는 1을 빼자 왜?? 헤더를 제외해야 하니까
    // ex) 리사이클러뷰에서 5번째 아이템은 아이템어레이에선 4번째다.
    private FeedItem getItem(int position) {
        return ITEMS.get(position - 1);
    }


    public interface ClickListener {

        void onPositionClicked(int position);

        void onLongClicked(int position);
    }

    // 뷰홀더와 프래그먼트 아이템을 매칭시키는 함수
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_ITEM) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_newsfeed_item, parent, false);
            //inflate your layout and pass it to view holder
            return new VHItem(view);
        } else if (viewType == TYPE_HEADER) {

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_newsfeed_layoutadd, parent, false);

            //inflate your layout and pass it to view holder
            return new VHHeader(view);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");


    }

    //뷰홀더가 아이템에 달라붙을 때 어떤 일을 할지 정의하는 함수
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHItem) {

            // 리사이클러뷰 아이템에 각 뷰를 세팅한다(작가, 단어, 뜻, 날짜 등)



            final VHItem itemHolder = (VHItem) holder;

            final FeedItem item = getItem(position);
            final Context context = itemHolder.mView.getContext();

            itemHolder.mWriterView.setText("" + item.writer);
            itemHolder.mIdView.setText("단어 : " + item.title);
            itemHolder.mContentView.setText("뜻 : " + item.content);
            itemHolder.mDateView.setText(item.writtenDate);

            itemHolder.textViewLikeFeed.setText("좋아요 " + item.likeFeedNum + "개");

            //댓글이 0개면 댓글 개수를 표시하지 않는다.
            if(item.commentNum == 0){

                itemHolder.mCommentNum.setVisibility(View.GONE);
            }
            //1개 이상이다. 댓글 개수를 표시한다.
            else{
                itemHolder.mCommentNum.setText("댓글 " + item.commentNum + "개");

            }




            SharedPreferences pref_login =  context.getSharedPreferences(context.getString(R.string.key_pref_Login), Context.MODE_PRIVATE );
            final String loginEmail = pref_login.getString("cookie_email","");

            Log.e("hoxy", "onBindViewHolder: "+loginEmail );

            //cast holder to VHItem and set data


            // 조건문이 나뉘는 경우 - 이미지 Url, 프로필Url은 없을 수도 있으니까.

            //이미지 Url의 유무에 따라 이미지뷰를 세팅함. 이미지 없으면 -> 기본 이미지를 세팅함
            if (!Objects.equals(item.imgUrl, "")) {

//            Log.e("imgUrl", "onBindViewHolder: "+getItem(position).imgUrl );

                Glide.with(itemHolder.mView).load(getItem(position).imgUrl).into(itemHolder.mImageView);
            } else {
                Glide.with(itemHolder.mView).load(R.drawable.ic_launcher_round).into(itemHolder.mImageView);
            }

            // 프로필 이미지의 유무에 따라 이미지뷰 세팅. 없으면 -> 기본 세팅
            if (!getItem(position).imgProfileUrl.equals("") && getItem(position).imgProfileUrl != null) {
                RequestOptions requestOptions = new RequestOptions()
                        .error(R.drawable.com_facebook_profile_picture_blank_square);


                Glide.with(itemHolder.mView).load(getItem(position).imgProfileUrl)
                        .apply(requestOptions)

                        .into(itemHolder.mProfileView);


            } else {

                Glide.with(itemHolder.mView).load(R.drawable.com_facebook_profile_picture_blank_square).into(itemHolder.mProfileView);
            }


            /*
            좋아요 버튼에 리스너를 세팅
             */

             /*
            체크 상태가 변화하고 이 함수로 들어간다.

            바뀌어서 체크된 상태다 -> 좋아요 적용
            바뀌어서 체크가 안된 상태 -> 좋아요 해제
             */
            itemHolder.buttonLikeFeed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                    if (isChecked) {
                        Toast.makeText(buttonView.getContext(), "좋아요 적용" + item.getFeedID(), Toast.LENGTH_SHORT).show();
                        itemHolder.buttonLikeFeed.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likegrey, 0, 0, 0);

                        item.likeFeedNum++;
                        itemHolder.textViewLikeFeed.setText("좋아요 " + item.likeFeedNum + "개");

                    } else { //optional + , your preference on what to to :)

                        Toast.makeText(buttonView.getContext(), "좋아요 취소" + item.getFeedID(), Toast.LENGTH_SHORT).show();
                        itemHolder.buttonLikeFeed.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likewhite, 0, 0, 0);

                        item.likeFeedNum--;
                        itemHolder.textViewLikeFeed.setText("좋아요 " + item.likeFeedNum + "개");
                    }


                }

            });


            /*
            더보기 버튼에 리스너를 세팅한다.

            사용자 계정이 작성자 계정(이메일)과 같으면
            수정 / 삭제가 포함된 다이얼로그를 띄우고,
            다르면 신고만 포함된 다이얼로그를 띄운다.
             */
            itemHolder.mMoreView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Context context = v.getContext();


                    // 더보기 버튼을 누른 경우
                    if (v.getId() == itemHolder.mMoreView.getId()) {

                        AlertDialog.Builder builderItem = new AlertDialog.Builder(context);

                                // 여기서 분기를 가른다.
                                // 작성자 이메일 = 쉐어드에 담긴 로그인 이메일이면 수정삭제 가능
                                // 아니면 수정 삭제 불가. else문에서 신고만 보이게 해라

                        //로그인 이메일과 게시물의 작성자가 같음 -> 수정삭제 세팅
                        if(loginEmail.equals(item.writerEmail)){

                            builderItem.setItems(R.array.edit_del, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item

                                    switch (which) {
                                        case 0:

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
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                                    .setMessage("정말 삭제하시겠습니까?")
                                                    .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {


                                                            DeleteFeedTask deleteFeedTask = new DeleteFeedTask(context);
                                                            deleteFeedTask.execute(item.getFeedID());


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

                        }

                        //로그인한 사람과 게시물 작성자 다름 -> 신고 버튼만 세팅됨
                        else{
                            builderItem.setItems(R.array.edit_another, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // The 'which' argument contains the index position
                                    // of the selected item

                                    switch (which) {
                                        case 0:

                                        Toast.makeText(context, "신고를 클릭했습니다. " + String.valueOf(item.getInfo()), Toast.LENGTH_SHORT).show();
                                            break;

                                    }
                                }
                            })
                                    .show();
                        }


                    }

                }
            });




        } else if (holder instanceof VHHeader) {
            //cast holder to VHHeader and set data for header.
        }


    }


    public class VHHeader extends RecyclerView.ViewHolder implements View.OnTouchListener {


        @BindView(R.id.cardViewAdd)
        CardView cardViewAdd;

        public VHHeader(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            cardViewAdd.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final Context context = v.getContext();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
//                        Toast.makeText(getContext(), "글을씁시다", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, WriteActivity.class);
                    context.startActivity(intent);
            }
            return false;
        }
//        @Override
//            public boolean onTouch(View v, MotionEvent event) {
//
//
//
//                return false;
//            }


    }


    public class VHItem extends RecyclerView.ViewHolder implements View.OnClickListener {
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

        @BindView(R.id.layoutWriteComment)
        RelativeLayout mLayoutWriteComment;

        @BindView(R.id.textViewCommentNum)
        TextView mCommentNum;

        @BindView(R.id.buttonLikeFeed)
        ToggleButton buttonLikeFeed;

        @BindView(R.id.textViewLikeFeed)
        TextView textViewLikeFeed;

        public FeedItem mItem;


        public VHItem(View view) {
            super(view);

            ButterKnife.bind(this, view);

            mView = view;

            mProfileView.setBackground(new ShapeDrawable(new OvalShape()));
            mProfileView.setClipToOutline(true);

//            mMoreView.setOnClickListener(this);
            mView.setOnClickListener(this);
            mLayoutWriteComment.setOnClickListener(this);

            buttonLikeFeed.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        @Override
        public void onClick(View v) {
            final Context context = v.getContext();

            // 더보기 버튼 이외의 클릭에 대해서는 댓글달기로 이동한다.
            if(v.getId() != mMoreView.getId() && v.getId() != buttonLikeFeed.getId() ){
                FeedItem item = ITEMS.get(getAdapterPosition() - 1);

                Intent intent = new Intent(v.getContext(), CommentActivity.class);
                intent.putExtra("feedID", item.getFeedID());
//                NewsFeedFragment.isWentCommentActivity = true;
                v.getContext().startActivity(intent);
//                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }


        }
    }


    String json_result = "";

    public class DeleteFeedTask extends AsyncTask<Integer, String, String> {

        private Context mContext;

        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.

        public DeleteFeedTask(Context context) {
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

            NewsFeedFragment.isItemDeleted = true;
            //삭제가 완료되었다. 여기서 새로고침을 진행하자
            //메인액티비티는 뷰페이저로 구성되어 있으므로, 페이저 어댑터를 불러와서 notify 시킨다.
            // 대댓글과 댓글은 다른 방식으로 새로고침이 구현되어 있다.
            MainActivity activity = (MainActivity) mContext;
            activity.getPagerAdapter().notifyDataSetChanged();


            Log.e("wow", result);


        }

    }

}



