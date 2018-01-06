package nova.typoapp.worditem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.R;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import nova.typoapp.worditem.WordItemContent.WordItem;
import nova.typoapp.wordset.WordSetActivity;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;
import static nova.typoapp.retrofit.ApiService.API_URL;

/**
 * Created by Administrator on 2018-01-03.
 */

public class MyWordItemRecyclerViewAdapter extends RecyclerView.Adapter<MyWordItemRecyclerViewAdapter.ViewHolder> {

    private final List<WordItem> mValues;


    public MyWordItemRecyclerViewAdapter(List<WordItem> items) {
        mValues = items;

    }



    TextToSpeech tts =new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if(status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.ENGLISH);
            }
        }
    });


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_word_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        final WordItem item = holder.mItem;

        holder.tvNameWord.setText(item.nameWord);

        holder.tvMeanWord.setText(item.meanWord);


        // 프로필 이미지의 유무에 따라 프로필 이미지뷰 세팅. 없으면 -> 기본 세팅


        Log.e("abc", "onBindViewHolder: "+item.UrlWordImg );
        if (item.UrlWordImg != null && !item.UrlWordImg.equals("")) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌
            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.com_facebook_profile_picture_blank_square);

            Glide.with(holder.mView).load(item.UrlWordImg)
                    .apply(requestOptions)
                    .into(holder.imgWord);
        }



        View.OnClickListener onWordClickListener = new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                //댓글 달기 버튼 혹은 댓글 갯수를 클릭했다면 댓글 액티비티로 이동시킨다.

                int clickedViewId = v.getId();

                if (clickedViewId == holder.imgSoundWord.getId() ){

                    //발음 관련 변수들 초기화


//                        Toast.makeText(context, "speak clicked "+item.getTitle(), Toast.LENGTH_SHORT).show();

                    String text = item.nameWord;
//                        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();


                    //발음 실행. 주의사항 : 롤리팝 버전 이상에서만 가능. 추가 코드 필요

                    String utteranceId = this.hashCode() + "";
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                }
            }
        };

        holder.imgSoundWord.setOnClickListener(onWordClickListener);



        //단어장 내부의 단어를 롱클릭하여 삭제.
        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

//                Toast.makeText(holder.mView.getContext(), "삭제합니다", Toast.LENGTH_SHORT).show();


                final Context context = holder.mView.getContext();

                //다이얼로그 띄우기

                //단어장 삭제를 눌렀다.

                AlertDialog.Builder deleteConfirmBuilder = new AlertDialog.Builder(context)
                        .setMessage("단어를 단어장에서 삭제하시겠습니까?")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //삭제를 클릭했다. 삭제를 진행하는 태스크를 실행한다.

//                                DeleteSetTask deleteSetTask = new DeleteSetTask(context);
//                                deleteSetTask.execute(item.idWordSet);




                                int idWordSetInActivity;

                                WordSetActivity activity = (WordSetActivity) holder.mView.getContext();
                                idWordSetInActivity= activity.getIdWordSet();


                                DeleteWordInSetTask deleteWordInSetTask = new DeleteWordInSetTask(holder.mView.getContext(), idWordSetInActivity ,item.nameWord, item.idWord);
                                deleteWordInSetTask.execute();



                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog deleteConfirmDialog = deleteConfirmBuilder.create();
                deleteConfirmDialog.show();





                return false;
            }
        };


        holder.mView.setOnLongClickListener(longClickListener);









//        //아이템에 클릭 리스너 세팅
//
//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//            }
//        });
    }







    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public WordItem mItem;


        @BindView(R.id.tvNameWord)
        TextView tvNameWord;

        @BindView(R.id.tvMeanWord)
        TextView tvMeanWord;

        @BindView(R.id.imgWord)
        ImageView imgWord;

        @BindView(R.id.imgSoundWord)
        ImageView imgSoundWord;




        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;

            Glide.with(view.getContext()).load(R.drawable.ic_volume_up_black_24dp).into(imgSoundWord);


        }

    }



    public class DeleteWordInSetTask extends AsyncTask<Integer, String, String> {

        private Context mContext;



        int idWordSet;
        String nameWord;
        int idWord;


        String json_result;
        // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.


        public DeleteWordInSetTask(Context context        ) {
            mContext = context;
        }

        public DeleteWordInSetTask(Context mContext, int idWordSet, String nameWord, int idWord) {
            this.mContext = mContext;
            this.idWordSet = idWordSet;
            this.nameWord = nameWord;
            this.idWord = idWord;
        }

        @Override
        protected String doInBackground(Integer... integers) {

            //region//단어장 삭제하기 - DB상에서 단어장을 삭제한다.


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


            // 레트로핏 콜 객체를 만든다. 파라미터로 게시물의 ID값, 게시물의 타입을 전송한다.

             Call<ResponseBody> comment = apiService.deleteWordInSet(idWord, idWordSet);


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
            //단어장 액티비티를 불러오고, 단어장 액티비티의 updateWordList를 콜한다.
            //이 함수는 프래그먼트를 가져와서 프래그먼트 내부의 새로고침 함수를 다시 콜한다. -> 새로고침 됨.

            WordSetActivity activity = (WordSetActivity) mContext;
            activity.updateWordList();



            Log.e("wow", result);


        }

    }


}
