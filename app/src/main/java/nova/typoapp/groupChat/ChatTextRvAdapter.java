package nova.typoapp.groupChat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.R;
import nova.typoapp.groupChat.ChatTextContent.ChatItem;

/**
 * Created by Administrator on 2018-02-04.
 */

public class ChatTextRvAdapter extends RecyclerView.Adapter<ChatTextRvAdapter.ViewHolder> {

    private final List<ChatItem> mValues;

    public ChatTextRvAdapter(List<ChatItem> items) {
        mValues = items;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public ChatTextRvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())

                //새로 어댑터 생성할 경우 아래 레이아웃 교체하기
                .inflate(R.layout.fragment_chat_item, parent, false);
        return new ChatTextRvAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ChatTextRvAdapter.ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        Context mContext = holder.mView.getContext();

        final ChatItem item = holder.mItem;


        //채팅 아이템을 채팅 뷰에 세팅한다.

        //채팅 작성자 세팅
        holder.tvChatWriter.setText(item.chatWriterName);

        //채팅 텍스트 세팅
        holder.tvChatText.setText(item.chatText);

        //채팅 시간 세팅
        holder.tvChatTimeRight.setText(item.chatTime);
        holder.tvChatTimeLeft.setText(item.chatTime);
        //아직 채팅 시간이 미구현이므로, 뷰를 보이지 않게 세팅해둠


        //멤버 이미지 뷰를 세팅한다.
        if (item.chatWriterProfile != null && !item.chatWriterProfile.equals("")) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(holder.mView).load(item.chatWriterProfile)
                    .apply(requestOptions)
                    .into(holder.imgChatProfile);
        }


        //채팅 텍스트를 내가 작성한 것이라면 채팅 뷰의 세팅을 바꿔준다.(말풍선 색깔/위치 변경, 텍스트 색 변경, 프로필 사진 및 이름 제거)

        //내가 작성한 것 어떻게 확인? 로그인 정보와 작성자 이메일을 비교함
        //

        //로그인 정보가 담긴 쉐어드 프리퍼런스 호출
        SharedPreferences pref_login = holder.mView.getContext().getSharedPreferences(holder.mView.getContext().getString(R.string.key_pref_Login), 0);


        //쉐어드 프리퍼런스에 담긴 유저의 이메일을 가져와 비교
        String userEmail = pref_login.getString("cookie_email", "null");

        //같은 경우 -> 내가 작성한 글 -> (말풍선 색깔/위치 변경, 텍스트 색 변경, 프로필 사진 및 이름 제거)
        if (item.chatWriterEmail.equals(userEmail)) {






            //말풍선을 우측으로 옮기고 색깔 변경
            holder.layoutChatBody.setGravity(Gravity.END);

            //말풍선 색깔을 바꾸기 위해 드로어블을 먼저 설정함.
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.bubbleout);
            drawable = DrawableCompat.wrap(drawable);

            //백그운드 드로어블 색을 파랑색으로 결정
            DrawableCompat.setTint(drawable, mContext.getResources().getColor(R.color.colorPrimaryDark));


            //말풍선의 백그라운드 설정
            holder.layoutChatBubble.setBackground(drawable);


            //글자색 설정
            holder.tvChatText.setTextColor(holder.mView.getContext().getResources().getColor(R.color.white));


            //좌측의 시간을 보이게, 우측 시간 안 보이게
            holder.tvChatTimeLeft.setVisibility(View.VISIBLE);
            holder.tvChatTimeRight.setVisibility(View.GONE);

            //프로필 사진 및 작성자 이름 뷰 제거
            holder.tvChatWriter.setVisibility(View.GONE);
            holder.imgChatProfile.setVisibility(View.GONE);






       }
        //타인 작성
        else{


            //말풍선을 좌측으로
            holder.layoutChatBody.setGravity(Gravity.START);

            //말풍선 모양을 바꾸기 위해 드로어블을 먼저 설정함.
            Drawable drawable = mContext.getResources().getDrawable(R.drawable.bubblein);
            drawable = DrawableCompat.wrap(drawable);

            //드로어블 색을 회색으로 결정
            DrawableCompat.setTint(drawable, mContext.getResources().getColor(R.color.borderGrey));

            //말풍선의 백그라운드 설정
            holder.layoutChatBubble.setBackground(drawable);


            //글자색 설정 - 진회색
            holder.tvChatText.setTextColor(holder.mView.getContext().getResources().getColor(R.color.fontGrey));


            //좌측 시간을 안 보이게, 우측 시간 보이게
            holder.tvChatTimeLeft.setVisibility(View.GONE);
            holder.tvChatTimeRight.setVisibility(View.VISIBLE);

            //프로필 사진 및 작성자 이름 뷰 제거
            holder.tvChatWriter.setVisibility(View.VISIBLE);
            holder.imgChatProfile.setVisibility(View.VISIBLE);



        }


        // 프로필 이미지의 유무에 따라 프로필 이미지뷰 세팅. 없으면 -> 기본 세팅


        final int viewId = holder.mView.getId();

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public ChatItem mItem;


        //채팅 몸체 : 그래비티를 조정하여 말풍선을 좌우로 움직일 수 있음
        @BindView(R.id.layoutChatBody)
        LinearLayout layoutChatBody;



        //채팅 프로필 이미지
        @BindView(R.id.imgChatProfile)
        ImageView imgChatProfile;


        //채팅 작성자
        @BindView(R.id.tvChatWriter)
        TextView tvChatWriter;

        //채팅 말풍선
        @BindView(R.id.layoutChatBubble)
        LinearLayout layoutChatBubble;

        //채팅 내용
        @BindView(R.id.tvChatText)
        TextView tvChatText;

        //채팅 작성 시간 - 좌측, 내가 작성했을 때 보여진다
        @BindView(R.id.tvChatTimeLeft)
        TextView tvChatTimeLeft;


        //채팅 작성 시간 - 우측, 타인이 작성했을 때 보여진다.
        @BindView(R.id.tvChatTimeRight)
        TextView tvChatTimeRight;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;


        }

    }


}

