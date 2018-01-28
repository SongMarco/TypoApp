package nova.typoapp.groupMember;

import android.os.Build;
import android.support.annotation.RequiresApi;
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
import nova.typoapp.groupMember.GroupMemberContent.MemberItem;

/**
 * Created by Administrator on 2018-01-29.
 */

public class MyGroupMemberAdapter extends RecyclerView.Adapter<MyGroupMemberAdapter.ViewHolder> {

    private final List<MemberItem> mValues;

    public MyGroupMemberAdapter(List<MemberItem> items) {
        mValues = items;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public MyGroupMemberAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())

                //새로 어댑터 생성할 경우 아래 레이아웃 교체하기
                .inflate(R.layout.fragment_likeritem, parent, false);
        return new MyGroupMemberAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyGroupMemberAdapter.ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        final MemberItem item = holder.mItem;

        //멤버 이름을 세팅한다.
        holder.textViewLiker.setText(item.nameMember);

        // 프로필 이미지의 유무에 따라 프로필 이미지뷰 세팅. 없으면 -> 기본 세팅


        //멤버 이미지 뷰를 세팅한다.
        if (item.imgUrlMEmber != null && !item.imgUrlMEmber.equals("")) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(holder.mView).load(item.imgUrlMEmber)
                    .apply(requestOptions)
                    .into(holder.likerProfileImg);
        }

        final int viewId = holder.mView.getId();

        //클릭 이벤트 주석 처리


//        View.OnClickListener clickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                int idClicked = view.getId();
//
//                if (idClicked == holder.mView.getId()) {
//
////                Toast.makeText(view.getContext(), "아이템 클릭 : "+item.nameGroup, Toast.LENGTH_SHORT).show();
//
//                    Intent intent = new Intent(view.getContext(), GroupActivity.class);
//
//                    // 그룹 액티비티를 시작한다.
//                    view.getContext().startActivity(intent);
//
//                }
//
//                if (idClicked == holder.imgEditGroup.getId()) {
//
//                    Toast.makeText(view.getContext(), "그룹을 수정하세요", Toast.LENGTH_SHORT).show();
//
//                    //로그인 이메일과 게시물의 작성자가 같음 -> 수정삭제 세팅
////
////
////                    final Context context = view.getContext();
////                    AlertDialog.Builder builderItem = new AlertDialog.Builder(context);
////
////                    builderItem.setItems(R.array.edit_set, new DialogInterface.OnClickListener() {
////                        public void onClick(DialogInterface dialog, int which) {
////                            // The 'which' argument contains the index position
////                            // of the selected item
////
////                            switch (which) {
////                                case 0:
////
//////                                      Toast.makeText(context, "수정 클릭 = " + item.nameWordSet, Toast.LENGTH_SHORT).show();
////                                    //수정을 클릭했다. 수정 다이얼로그를 띄운다.
////
////                                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
////
////                                    builder.setTitle("단어장 이름 수정");       // 제목 설정
//////        builder.setMessage("");   // 내용 설정
////
////// EditText 삽입하기
////                                    final EditText etNameWordSet = new EditText(context);
////
////
////                                    //초기값 - 단어장 제목을 세팅해줌
////
////                                    etNameWordSet.setHint(R.string.hint_etNameWordSet);
////                                    etNameWordSet.setText(item.nameWordSet);
////                                    etNameWordSet.setSelection(etNameWordSet.getText().length());
////
////
////
////                                    LinearLayout layout = new LinearLayout(context);
////                                    layout.setOrientation(LinearLayout.VERTICAL);
////
////                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
////                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
////                                    params.setMargins(60, 0, 60, 0);
////
////
////                                    layout.addView(etNameWordSet, params);
////
////
////                                    builder.setView(layout);
////
////
////                                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
////                                        @Override
////                                        public void onClick(DialogInterface dialog, int which) {
////
////
////                                            Toast.makeText(context, "단어장 이름이 수정되었습니다.", Toast.LENGTH_SHORT).show();
//////                                                // Text 값 받아서 로그 남기기
//////
////                                            // 에딧텍스트에 제목 입력을 했다면 서버로 입력 정보를 보내고, 단어장을 수정한다.
////                                            if(etNameWordSet.getText()!=null &&!etNameWordSet.getText().toString().equals("")){
////                                                String nameWordSet = etNameWordSet.getText().toString();
////
////
////
////
////
////                                                MyWordSetItemRecyclerViewAdapter.EditSetTask editSetTask = new MyWordSetItemRecyclerViewAdapter.EditSetTask(context, nameWordSet, item.idWordSet);
////                                                editSetTask.execute();
////
////
////
////                                            }
////                                            else{
////                                                Toast.makeText(context, "에러 : 단어장 이름을 입력하지 않으셨네요!", Toast.LENGTH_SHORT).show();
////                                            }
////
//////                                                nameWordSet = etNameWordSet.getText().toString();
//////                                                Log.v(TAG, "nameWordSet = "+nameWordSet);
////
////                                            dialog.dismiss();     //닫기
////                                            // Event
////                                        }
////                                    });
////
////// 중립 버튼 설정
////                                    builder.setNeutralButton("취소", new DialogInterface.OnClickListener() {
////                                        @Override
////                                        public void onClick(DialogInterface dialog, int which) {
////
////                                            dialog.dismiss();     //닫기
////                                            // Event
////                                        }
////                                    });
////
////                                    builder.show();
////
////
////
////
////
////                                    break;
////                                case 1:
////
////                                    //단어장 삭제를 눌렀다.
////
////                                    AlertDialog.Builder deleteConfirmBuilder = new AlertDialog.Builder(context)
////                                            .setMessage("정말 삭제하시겠습니까?")
////                                            .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
////                                                @Override
////                                                public void onClick(DialogInterface dialog, int which) {
////
//////                                                        Toast.makeText(context, "삭제 ㄱㄱ = " + item.nameWordSet, Toast.LENGTH_SHORT).show();
////                                                    MyWordSetItemRecyclerViewAdapter.DeleteSetTask deleteSetTask = new MyWordSetItemRecyclerViewAdapter.DeleteSetTask(context);
////                                                    deleteSetTask.execute(item.idWordSet);
////
////
////                                                }
////                                            })
////                                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
////                                                @Override
////                                                public void onClick(DialogInterface dialog, int which) {
////                                                    dialog.dismiss();
////                                                }
////                                            });
////
////                                    AlertDialog deleteConfirmDialog = deleteConfirmBuilder.create();
////                                    deleteConfirmDialog.show();
////
//////                                        ITEMS.remove(item);
//////
////
//////                                        Toast.makeText(context, "삭제 클릭 = " + String.valueOf(ITEMS.get(getAdapterPosition()).getInfo()), Toast.LENGTH_SHORT).show();
////
////                                    break;
////
////                            }
////                        }
////                    })
////                            .show();
//
//                }
//
//
//            }
//
//
//        };

//        //아이템에 클릭 리스너 세팅
//        holder.mView.setOnClickListener(clickListener);
//        holder.imgEditGroup.setOnClickListener(clickListener);


    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public MemberItem mItem;


        @BindView(R.id.likerProfileImg)
        ImageView likerProfileImg;

        @BindView(R.id.textViewLiker)
        TextView textViewLiker;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;


        }

    }


}

