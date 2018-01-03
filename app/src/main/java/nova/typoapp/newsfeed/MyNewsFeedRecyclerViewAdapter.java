package nova.typoapp.newsfeed;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.CommentActivity;
import nova.typoapp.LikeListActivity;
import nova.typoapp.MainActivity;
import nova.typoapp.NewsFeedFragment;
import nova.typoapp.NewsFeedFragment.OnListFragmentInteractionListener;
import nova.typoapp.R;
import nova.typoapp.WriteActivity;
import nova.typoapp.newsfeed.NewsFeedContent.FeedItem;
import nova.typoapp.notificationlist.NoticeClickedActivity;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static android.view.View.GONE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.facebook.FacebookSdk.getApplicationContext;
import static nova.typoapp.newsfeed.NewsFeedContent.ITEMS;
import static nova.typoapp.retrofit.ApiService.API_URL;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FeedItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 *
 */

/*
 리사이클러뷰의 어댑터다. 300줄가량 되는 복잡한 코드이니 주석을 잘 작성하도록 하자
 참고한 코드는 스택오버플로우
 https://stackoverflow.com/questions/30284067/handle-button-click-inside-a-row-in-recyclerview
 */




public class MyNewsFeedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {


    EndlessScrollListener endlessScrollListener;

    public interface EndlessScrollListener {

        /**
         * Loads more data.
         * @param position
         * @return true loads data actually, false otherwise.
         */
        boolean onLoadMore(int position);


    }
//    /*
//    데이터를 더 불러오는 메소드
//     */
//    @Override
//    public boolean onLoadMore(int position) {
//
//
//        FeedItem item = ITEMS.get(9);
//
//        for(int i = 0; i < 5; i++){
//            ITEMS.add(item);
//        }
//
//
//
//        return false;
//
//
//    }



    TextToSpeech tts =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if(status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.ENGLISH);
            }
        }
    });

    public EndlessScrollListener getEndlessScrollListener() {
        return endlessScrollListener;
    }

    public void setEndlessScrollListener(EndlessScrollListener endlessScrollListener) {
        this.endlessScrollListener = endlessScrollListener;
    }


    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // 아이템들의 리스트를 만든다.
    private final List<FeedItem> mValues;

    // 아이템 요소를 클릭할 때 반응할 클릭 리스너다.
    // 이 리스너는 바깥에서 이 리사이클러뷰를 호출할 때 사용된다.
    // 내 코드에서는 클릭한 아이템을 이 클래스에서 곧바로 활용 가능하므로,
    // 적용하지 않았다. -> 보충 필요
    private ClickListener listener;



    /*
    리사이클러뷰 생성자들.
     */

    public MyNewsFeedRecyclerViewAdapter(List<FeedItem> items, ClickListener clickListener) {
        mValues = items;
        this.listener = clickListener;
    }

    public MyNewsFeedRecyclerViewAdapter(List<FeedItem> items) {
        mValues = items;

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

        else return TYPE_ITEM;
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

            itemHolder.textViewLikeFeed.setText("좋아요 " + item.likeFeedNum );

            //댓글이 0개면 댓글 개수를 표시하지 않는다.
            if(item.commentNum == 0){

                itemHolder.mCommentNum.setVisibility(GONE);
            }
            //1개 이상이다. 댓글 개수를 표시한다.
            else{
                itemHolder.mCommentNum.setVisibility(View.VISIBLE);
                itemHolder.mCommentNum.setText("댓글 " + item.commentNum );

            }

            // you can cache getItemCount() in a member variable for more performance tuning
            final int VISIBLE_THRESHOLD = 5;
            //일단 뒤에서 3번째 아이템에 닿았을 때 아이템을 추가하도록 설정

            int realCount = getItemCount()-1;
            Log.e("position", "onBindViewHolder: position "+position );
            Log.e("position", "onBindViewHolder: count "+(getItemCount()-1) );
            if(position == getItemCount()- 1) {

//                    Toast.makeText(context, "load more", Toast.LENGTH_SHORT).show();

                    endlessScrollListener.onLoadMore(position);



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
            좋아요 버튼 세팅
             */

            //이미 좋아요 한 버튼임 -> 좋아요모양 세팅
            if(item.isLiked.equals("true")){
                itemHolder.buttonLikeFeed.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likegrey, 0, 0, 0);
                itemHolder.buttonLikeFeed.setChecked(true);
            }
            //좋아요 안한 버튼임
            else{

                itemHolder.buttonLikeFeed.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likewhite, 0, 0, 0);
                itemHolder.buttonLikeFeed.setChecked(false);

            }


            /*
            좋아요 버튼에 setOnCheckedChangeListener 리스너를 세팅
             */

             /*
            체크 상태가 변화하고 이 함수로 들어간다.

            바뀌어서 체크된 상태다 -> 좋아요 적용
            바뀌어서 체크가 안된 상태 -> 좋아요 해제
             */
            itemHolder.buttonLikeFeed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Boolean isChecked = ((ToggleButton)v).isChecked();

                    if (isChecked) {
//                        Toast.makeText(buttonView.getContext(), "좋아요 적용" + item.getFeedID(), Toast.LENGTH_SHORT).show();
                        itemHolder.buttonLikeFeed.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likegrey, 0, 0, 0);

                        item.likeFeedNum++;
                        itemHolder.textViewLikeFeed.setText("좋아요 " + item.likeFeedNum + "개");

                        ApplyLikeFeedTask likeFeedTask = new ApplyLikeFeedTask(context);
                        likeFeedTask.execute(item.feedID);

                    } else { //optional + , your preference on what to to :)

//                        Toast.makeText(buttonView.getContext(), "좋아요 취소" + item.getFeedID(), Toast.LENGTH_SHORT).show();
                        itemHolder.buttonLikeFeed.setCompoundDrawablesWithIntrinsicBounds(R.drawable.likewhite, 0, 0, 0);

                        item.likeFeedNum--;
                        itemHolder.textViewLikeFeed.setText("좋아요 " + item.likeFeedNum + "개");

                        ApplyLikeFeedTask likeFeedTask = new ApplyLikeFeedTask(context);
                        likeFeedTask.execute(item.feedID);

                    }
                }


            });

            /*

            댓글 갯수와 댓글 달기에 리스너를 세팅하여,
            클릭시 댓글 액티비티로 이동시킨다.

            아이템에 이 리스너를 세팅해야 리스너가 작동함에 유의.

             */

            View.OnClickListener mFeedClickListener = new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View v) {

                    //댓글 달기 버튼 혹은 댓글 갯수를 클릭했다면 댓글 액티비티로 이동시킨다.

                    int clickedViewId = v.getId();

                    if(  clickedViewId== itemHolder.mCommentNum.getId() || clickedViewId == itemHolder.mLayoutWriteComment.getId() ) {
                        Intent intent = new Intent(v.getContext(), CommentActivity.class);
                        intent.putExtra("feedID", item.getFeedID());

                        //좋아요를 누른 상태라면 isLiked 를 true 로 세팅해 보내서, 댓글에서도 좋아요를 적용한다.
                        if( itemHolder.buttonLikeFeed.isChecked() ){
                            intent.putExtra("isLiked", true);
                        }
                        else{
                            intent.putExtra("isLiked", false);
                        }

                        intent.putExtra("likeNum", item.likeFeedNum);

                        intent.putExtra("wordName", item.title );
                        intent.putExtra("emailFeedWriter", item.writerEmail);

                        v.getContext().startActivity(intent);
                    }

                    // 발음 기호를 클릭했다.
                    // tts로 영어 발음을 하도록 해준다.


                    if(clickedViewId == itemHolder.imageViewSound.getId() ){

                        //발음 관련 변수들 초기화


//                        Toast.makeText(context, "speak clicked "+item.getTitle(), Toast.LENGTH_SHORT).show();

                        String text = item.title;
//                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();


                        //발음 실행. 주의사항 : 롤리팝 버전 이상에서만 가능. 추가 코드 필요

                        String utteranceId=this.hashCode() + "";
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);


                    }


                    //사전 모양을 클릭했다.
                    //다음 사전으로 보내준다.

                    if(clickedViewId == itemHolder.imageViewDic.getId() ){


//                        String dicUrl = "http://dic.daum.net/search.do?q="+item.getTitle();

                        String dicUrl = "https://m.search.naver.com/search.naver?query="+item.getTitle();

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(dicUrl));

                       context.startActivity(intent);




                    }

                    // 단어장에 추가하기 버튼을 눌렀다.
                    // 단어장 리스트를 열어, 단어장을 선택하게 한다.
                    if(clickedViewId == itemHolder.mLayoutAddToSet.getId() ){


//
                        Toast.makeText(context, "단어장을 선택함", Toast.LENGTH_SHORT).show();

                        //단어장 리스트를 열어야 한다.

                        //itembuilder.setAdapter 로 리스트뷰 세팅 가능



                    }






                }
            };

            itemHolder.mCommentNum.setOnClickListener(mFeedClickListener);
            itemHolder.mLayoutWriteComment.setOnClickListener(mFeedClickListener);
            itemHolder.imageViewSound.setOnClickListener(mFeedClickListener);
            itemHolder.imageViewDic.setOnClickListener(mFeedClickListener);

            itemHolder.mLayoutAddToSet.setOnClickListener(mFeedClickListener);






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


            ViewGroup.LayoutParams param= ((VHHeader) holder).cardViewAdd.getLayoutParams();

            Log.e("context", "onBindViewHolder: "+ holder.itemView.getContext().getClass().getSimpleName() );

            if(holder.itemView.getContext().getClass().getSimpleName().equals(NoticeClickedActivity.class.getSimpleName() ) ){

                ((VHHeader) holder).cardViewAdd.setVisibility(GONE);
                param.height = 0;
                param.width = 0;
            }

            if( holder.itemView.getContext().getClass().getSimpleName().equals(MainActivity.class.getSimpleName() ))
            {

                ((VHHeader) holder).cardViewAdd.setVisibility(View.VISIBLE);
                param.height = WRAP_CONTENT ;
                param.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }



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
        ImageView mImageView;

        @BindView(R.id.imageProf)
        ImageView mProfileView;

        @BindView(R.id.imageViewMore)
        ImageView mMoreView;

        @BindView(R.id.layoutWriteComment)
        RelativeLayout mLayoutWriteComment;

        @BindView(R.id.layoutAddToSet)
        RelativeLayout mLayoutAddToSet;


        @BindView(R.id.textViewCommentNum)
        TextView mCommentNum;

        @BindView(R.id.buttonLikeFeed)
        ToggleButton buttonLikeFeed;

        @BindView(R.id.textViewLikeFeed)
        TextView textViewLikeFeed;

        @BindView(R.id.imageViewSound)
        ImageView imageViewSound;

        @BindView(R.id.imageViewDic)
        ImageView imageViewDic;


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
            textViewLikeFeed.setOnClickListener(this);

            Glide.with(mView.getContext()).load(R.drawable.ic_volume_up_black_24dp).into(imageViewSound);
            Glide.with(mView.getContext()).load(R.drawable.dic).into(imageViewDic);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        @Override
        public void onClick(View v) {
            final Context context = v.getContext();

            FeedItem item = ITEMS.get(getAdapterPosition() - 1);



            if(v.getId() == textViewLikeFeed.getId()){

//                Toast.makeText(v.getContext(), "좋아요 갯수 클릭 = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), LikeListActivity.class);
                intent.putExtra("feedID", item.getFeedID());
//                NewsFeedFragment.isWentCommentActivity = true;
                v.getContext().startActivity(intent);
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


    public class ApplyLikeFeedTask extends AsyncTask<Integer, String, String> {

        private Context mContext;

        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.

        public ApplyLikeFeedTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(Integer... integers) {

            //region//글 삭제하기 - DB상에서 뉴스피드 글을 삭제한다.
            //레트로핏 기초 컴포넌트 만드는 과정. 자주 복붙할 것.
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new ReceivedCookiesInterceptor(mContext))
                    .addInterceptor(new AddCookiesInterceptor(mContext))
                    .addInterceptor(httpLoggingInterceptor)
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(okHttpClient)
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
//            Log.e("myimg", "doInBackground: " + uploadImagePath);

            String type = "feed";
            Call<ResponseBody> comment = apiService.likeFeed(integers[0], type );


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


            //새로고침을 적용해야 한다. 어떻게 할지 고민해보라
            //좋아요 갯수 증가는 changed 리스너에서 처리했다.



//            MainActivity activity = (MainActivity) mContext;
//            activity.getPagerAdapter().notifyDataSetChanged();


            Log.e("wow", result);


        }

    }

}



