package nova.typoapp.comment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.CommentActivity;
import nova.typoapp.CommentFragment.OnListFragmentInteractionListener;
import nova.typoapp.EditCommentActivity;
import nova.typoapp.R;
import nova.typoapp.SubCommentActivity;
import nova.typoapp.comment.CommentContent.CommentItem;
import nova.typoapp.retrofit.ApiService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;

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
    public void onBindViewHolder(final ViewHolder itemHolder, int position) {
        itemHolder.mItem = mValues.get(position);

        final CommentItem item = itemHolder.mItem;

        final Context context = itemHolder.mView.getContext();

//        holder.mIdView.setText(mValues.get(position).id);
//        holder.mContentView.setText(mValues.get(position).content);

        itemHolder.mCommentContentView.setText(item.commentContent);

        itemHolder.mCommentWriterView.setText(item.commentWriter);
        itemHolder.mCommentDateView.setText(item.commentDate);

        SharedPreferences pref_login =  context.getSharedPreferences(context.getString(R.string.key_pref_Login), Context.MODE_PRIVATE );
        final String loginEmail = pref_login.getString("cookie_email","");

        if (item.subCommentNum == 0) {
            itemHolder.mSubCommentNum.setVisibility(View.GONE);
        }
        itemHolder.mSubCommentNum.setText("이전 답글" + item.subCommentNum + "개 보기");

        Log.e("onBindTag", "onBindViewHolder: writer= " + item.commentWriter + "content = " + item.commentContent);


// 프로필 이미지의 유무에 따라 이미지뷰 세팅. 없으면 -> 기본 세팅
        if (mValues.get(position).imgProfileUrl != null && !mValues.get(position).imgProfileUrl.equals("")) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(itemHolder.mView).load(mValues.get(position).imgProfileUrl)
                    .apply(requestOptions)
                    .into(itemHolder.mCommentProfileImage);
        }

        /*
            댓글 레이아웃에 롱 클릭 리스너를 세팅한다.

            사용자 계정이 작성자 계정(이메일)과 같으면
            수정 / 삭제가 포함된 다이얼로그를 띄우고,
            다르면 신고만 포함된 다이얼로그를 띄운다.
        */

        itemHolder.mLayoutCommentBody.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {





                // 레이아웃을 누른 경우
                if (v.getId() == itemHolder.mLayoutCommentBody.getId()) {

                    AlertDialog.Builder builderItem = new AlertDialog.Builder(context);

                    // 여기서 분기를 가른다.
                    // 작성자 이메일 = 쉐어드에 담긴 로그인 이메일이면 수정삭제 가능
                    // 아니면 수정 삭제 불가. else문에서 신고만 보이게 해라

                    //로그인 이메일과 게시물의 작성자가 같음 -> 수정삭제 세팅
                    if (loginEmail.equals(item.commentWriterEmail)) {

                        builderItem.setItems(R.array.edit_del, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item

                                switch (which) {
                                    case 0:

                                        Intent intent = new Intent(context, EditCommentActivity.class);

                                        intent.putExtra("content", item.commentContent);
                                        intent.putExtra("commentID", item.commentID);
                                        context.startActivity(intent);

//                                      Toast.makeText(context, "수정 클릭 = "+item.commentID, Toast.LENGTH_SHORT ).show();

                                        break;
                                    case 1:

                                        //현재 컨텍스트는 메인 액티비티이다. asynctask로 컨텍스트 전달 또한 가능하다.
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                                .setMessage("정말 삭제하시겠습니까?")
                                                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        DeleteCommentTask deleteCommentTask = new DeleteCommentTask(context);
                                                        deleteCommentTask.execute(item.commentID);
//                                                        Toast.makeText(context, "삭제 클릭 = "+item.commentID, Toast.LENGTH_SHORT ).show();

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
                    else {
                        builderItem.setItems(R.array.edit_another, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item

                                switch (which) {
                                    case 0:

                                        Toast.makeText(context, "신고를 클릭했습니다. " + String.valueOf(item.commentID ), Toast.LENGTH_SHORT).show();
                                        break;

                                }
                            }
                        })
                                .show();
                    }
                }


                return false;
            }
        });


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

        @BindView(R.id.textViewSubCommentNum)
        TextView mSubCommentNum;

        @BindView(R.id.layoutCommentBody)
        LinearLayout mLayoutCommentBody;


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
            mSubCommentNum.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            final Context context = v.getContext();

            // 답글보기 혹은 답글 갯수를 클릭하면 답글 액티비티로 이동시킨다.
            if (v.getId() == mCoComment.getId() || v.getId() == mSubCommentNum.getId()) {


                CommentItem item = CommentContent.ITEMS.get(getAdapterPosition());

                Intent intent = new Intent(v.getContext(), SubCommentActivity.class);
                intent.putExtra("commentID", item.commentID);
//                NewsFeedFragment.isWentCommentActivity = true;
                v.getContext().startActivity(intent);
//                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            } else {

            }
        }


//        @Override
//        public String toString() {
//            return super.toString() + " '" + mContentView.getText() + "'";
//        }
    }

    String json_result = "";

    public class DeleteCommentTask extends AsyncTask<Integer, String, String> {

        private Context mContext;

        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.

        public DeleteCommentTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(Integer... integers) {

            //region//글 삭제하기 - DB상에서 뉴스피드 글을 삭제한다.

            Retrofit retrofit = new Retrofit.Builder().baseUrl(API_URL).build();
            ApiService apiService = retrofit.create(ApiService.class);

            Call<ResponseBody> comment = apiService.deleteComment(integers[0]);


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

            //

            /*
            서버에서 댓글 삭제가 완료되었다. 댓글 리스트의 새로고침을 진행하자

            CommentActivity 를 컨텍스트에서 가져온 후,
            액티비티의 updateCommentList 메소드를 콜한다.

            이 메소드는 서버에서 댓글 리스트를 받아와 리사이클러뷰를 다시 세팅한다.

            따라서 새로고침이 완료된다.
             */
            CommentActivity commentActivity = (CommentActivity)mContext;
            commentActivity.updateCommentList();

//            CommentFragment commentFragment = (CommentFragment) commentActivity.getSupportFragmentManager().findFragmentById(R.id.fragmentCommentList);
//
//            if(commentFragment != null){
//
//                Toast.makeText(mContext, "update called", Toast.LENGTH_SHORT).show();
//                commentFragment.RefreshCommentTaskInFragment refreshCommentTaskInFragment = new CommentFragment.RefreshCommentTaskInFragment();
//
//            }



            Log.e("wow", result);


        }

    }
}
