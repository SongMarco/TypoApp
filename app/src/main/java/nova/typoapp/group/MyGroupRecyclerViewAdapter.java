package nova.typoapp.group;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
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

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.MainActivity;
import nova.typoapp.R;
import nova.typoapp.group.GroupContent.GroupItem;
import nova.typoapp.retrofit.ApiService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;

/**
 * Created by Administrator on 2018-01-27.
 */

public class MyGroupRecyclerViewAdapter extends RecyclerView.Adapter<MyGroupRecyclerViewAdapter.ViewHolder> {

    private final List<GroupItem> mValues;

    public MyGroupRecyclerViewAdapter(List<GroupItem> items) {
        mValues = items;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public MyGroupRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_group_item, parent, false);
        return new MyGroupRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyGroupRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        final GroupItem item = holder.mItem;

        holder.tvGroupName.setText(item.nameGroup);

        holder.tvNameGroupOwner.setText("운영자 : "+item.nameGroupOwner);

        holder.tvNumMembers.setText(item.numGroupMembers + " 명");

        // 프로필 이미지의 유무에 따라 프로필 이미지뷰 세팅. 없으면 -> 기본 세팅

        if (item.UrlGroupImg != null && !item.UrlGroupImg.equals("")) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(holder.mView).load(item.UrlGroupImg)
                    .apply(requestOptions)
                    .into(holder.imgGroup);
        }

        final int viewId = holder.mView.getId();

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int idClicked = view.getId();

                if (idClicked == holder.mView.getId()) {


//                Toast.makeText(view.getContext(), "아이템 클릭 : "+item.nameGroup, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(view.getContext(), GroupActivity.class);

                    intent.putExtra("idGroup",item.idGroup);

                    intent.putExtra("nameGroup", item.nameGroup);
                    intent.putExtra("contentGroup", item.contentGroup);
                    intent.putExtra("UrlGroupImg", item.UrlGroupImg);

                    intent.putExtra("numGroupMembers", item.numGroupMembers);





                    // 그룹 액티비티를 시작한다.
                    view.getContext().startActivity(intent);

                }

                if(idClicked == holder.imgEditGroup.getId()){

                    Toast.makeText(view.getContext(), "그룹을 수정하세요", Toast.LENGTH_SHORT).show();

                    //로그인 이메일과 게시물의 작성자가 같음 -> 수정삭제 세팅
//
//
//                    final Context context = view.getContext();
//                    AlertDialog.Builder builderItem = new AlertDialog.Builder(context);
//
//                    builderItem.setItems(R.array.edit_set, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            // The 'which' argument contains the index position
//                            // of the selected item
//
//                            switch (which) {
//                                case 0:
//
////                                      Toast.makeText(context, "수정 클릭 = " + item.nameWordSet, Toast.LENGTH_SHORT).show();
//                                    //수정을 클릭했다. 수정 다이얼로그를 띄운다.
//
//                                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
//
//                                    builder.setTitle("단어장 이름 수정");       // 제목 설정
////        builder.setMessage("");   // 내용 설정
//
//// EditText 삽입하기
//                                    final EditText etNameWordSet = new EditText(context);
//
//
//                                    //초기값 - 단어장 제목을 세팅해줌
//
//                                    etNameWordSet.setHint(R.string.hint_etNameWordSet);
//                                    etNameWordSet.setText(item.nameWordSet);
//                                    etNameWordSet.setSelection(etNameWordSet.getText().length());
//
//
//
//                                    LinearLayout layout = new LinearLayout(context);
//                                    layout.setOrientation(LinearLayout.VERTICAL);
//
//                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                                    params.setMargins(60, 0, 60, 0);
//
//
//                                    layout.addView(etNameWordSet, params);
//
//
//                                    builder.setView(layout);
//
//
//                                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//
//
//                                            Toast.makeText(context, "단어장 이름이 수정되었습니다.", Toast.LENGTH_SHORT).show();
////                                                // Text 값 받아서 로그 남기기
////
//                                            // 에딧텍스트에 제목 입력을 했다면 서버로 입력 정보를 보내고, 단어장을 수정한다.
//                                            if(etNameWordSet.getText()!=null &&!etNameWordSet.getText().toString().equals("")){
//                                                String nameWordSet = etNameWordSet.getText().toString();
//
//
//
//
//
//                                                MyWordSetItemRecyclerViewAdapter.EditSetTask editSetTask = new MyWordSetItemRecyclerViewAdapter.EditSetTask(context, nameWordSet, item.idWordSet);
//                                                editSetTask.execute();
//
//
//
//                                            }
//                                            else{
//                                                Toast.makeText(context, "에러 : 단어장 이름을 입력하지 않으셨네요!", Toast.LENGTH_SHORT).show();
//                                            }
//
////                                                nameWordSet = etNameWordSet.getText().toString();
////                                                Log.v(TAG, "nameWordSet = "+nameWordSet);
//
//                                            dialog.dismiss();     //닫기
//                                            // Event
//                                        }
//                                    });
//
//// 중립 버튼 설정
//                                    builder.setNeutralButton("취소", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//
//                                            dialog.dismiss();     //닫기
//                                            // Event
//                                        }
//                                    });
//
//                                    builder.show();
//
//
//
//
//
//                                    break;
//                                case 1:
//
//                                    //단어장 삭제를 눌렀다.
//
//                                    AlertDialog.Builder deleteConfirmBuilder = new AlertDialog.Builder(context)
//                                            .setMessage("정말 삭제하시겠습니까?")
//                                            .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//
////                                                        Toast.makeText(context, "삭제 ㄱㄱ = " + item.nameWordSet, Toast.LENGTH_SHORT).show();
//                                                    MyWordSetItemRecyclerViewAdapter.DeleteSetTask deleteSetTask = new MyWordSetItemRecyclerViewAdapter.DeleteSetTask(context);
//                                                    deleteSetTask.execute(item.idWordSet);
//
//
//                                                }
//                                            })
//                                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    dialog.dismiss();
//                                                }
//                                            });
//
//                                    AlertDialog deleteConfirmDialog = deleteConfirmBuilder.create();
//                                    deleteConfirmDialog.show();
//
////                                        ITEMS.remove(item);
////
//
////                                        Toast.makeText(context, "삭제 클릭 = " + String.valueOf(ITEMS.get(getAdapterPosition()).getInfo()), Toast.LENGTH_SHORT).show();
//
//                                    break;
//
//                            }
//                        }
//                    })
//                            .show();




                }


            }


        };

        //아이템에 클릭 리스너 세팅
        holder.mView.setOnClickListener(clickListener);
        holder.imgEditGroup.setOnClickListener(clickListener);


    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public GroupItem mItem;


        @BindView(R.id.tvNameGroupOwner)
        TextView tvNameGroupOwner;

        @BindView(R.id.tvNumMembers)
        TextView tvNumMembers;

        @BindView(R.id.imgGroup)
        ImageView imgGroup;

        @BindView(R.id.tvGroupName)
        TextView tvGroupName;

        @BindView(R.id.imgEditGroup)
        ImageView imgEditGroup;


        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;




        }

    }


    public class DeleteSetTask extends AsyncTask<Integer, String, String> {

        private Context mContext;

        String json_result;
        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.


        public DeleteSetTask(Context context        ) {
            mContext = context;
        }



        @Override
        protected String doInBackground(Integer... integers) {

            //region//단어장 삭제하기 - DB상에서 단어장을 삭제한다.

            Retrofit retrofit = new Retrofit.Builder().baseUrl(API_URL).build();
            ApiService apiService = retrofit.create(ApiService.class);

            Call<ResponseBody> comment = apiService.deleteWordSet(integers[0]);


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
            //메인 액티비티를 불러오고, 메인 액티비티의 updateWordSet를 콜한다.
            //이 함수는 프래그먼트를 가져와서 프래그먼트 내부의 새로고침 함수를 다시 콜한다. -> 새로고침 됨.

            MainActivity activity = (MainActivity) mContext;
            activity.updateWordSet();



            Log.e("wow", result);


        }

    }



    public class EditSetTask extends AsyncTask<Integer, String, String> {

        private Context mContext;

        String nameWordSet;
        int idWordSet;


        String json_result;
        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.

        public EditSetTask(Context context) {
            mContext = context;
        }

        public EditSetTask(Context mContext, String nameWordSet, int idWordSet) {
            this.mContext = mContext;
            this.nameWordSet = nameWordSet;
            this.idWordSet = idWordSet;
        }

        @Override
        protected String doInBackground(Integer... integers) {

            //region//단어장 삭제하기 - DB상에서 단어장을 삭제한다.

            Retrofit retrofit = new Retrofit.Builder().baseUrl(API_URL).build();
            ApiService apiService = retrofit.create(ApiService.class);

            Call<ResponseBody> comment = apiService.editWordSet(idWordSet, nameWordSet);


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
            //메인 액티비티를 불러오고, 메인 액티비티의 updateWordSet를 콜한다.
            //이 함수는 프래그먼트를 가져와서 프래그먼트 내부의 새로고침 함수를 다시 콜한다. -> 새로고침 됨.

            MainActivity activity = (MainActivity) mContext;
            activity.updateWordSet();



            Log.e("wow", result);


        }

    }





}
