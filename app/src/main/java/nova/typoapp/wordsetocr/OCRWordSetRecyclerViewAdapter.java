package nova.typoapp.wordsetocr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

/**
 * Created by Administrator on 2018-01-13.
 */

public class OCRWordSetRecyclerViewAdapter extends RecyclerView.Adapter<OCRWordSetRecyclerViewAdapter.ViewHolder> {

    private final List<OCRWordSetContent.WordItem> mValues;
    private CallbackInterface mCallback;
    private Context mContext;

    public OCRWordSetRecyclerViewAdapter(Context context, List<OCRWordSetContent.WordItem> items) {
        mValues = items;

        this.mContext = context;

        // .. Attach the interface
        try {
            mCallback = (CallbackInterface) context;
        } catch (ClassCastException ex) {
            //.. should log the error or throw and exception
            Log.e("MyAdapter", "Must implement the CallbackInterface in the Activity", ex);
        }
    }

    // OCRSelectWordActivity 와 소통하기 위한 인터페이스 - 사용하려는 액티비티에서 implement 할 것.
    public interface CallbackInterface {

        /**
         * 클릭하면 콜백이 실행된다.
         *
         * @param position - 리사이클러뷰 포지션
         * @param viewHolder  - 세팅하려는 아이템의 뷰홀더
         */
        void onHandleSelection(int position, OCRWordSetRecyclerViewAdapter.ViewHolder viewHolder);
    }


    //뷰홀더를 생성한다. -
    @Override
    public OCRWordSetRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())

                //레이아웃이 해당 리스트의 아이템인지를 확인할 것
                .inflate(R.layout.fragment_ocr_wordset_item, parent, false);
        return new OCRWordSetRecyclerViewAdapter.ViewHolder(view);
    }


    // 뷰홀더가 아이템과 매치된다.
    @Override
    public void onBindViewHolder(final OCRWordSetRecyclerViewAdapter.ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);

        //아이템을 세팅
        final OCRWordSetContent.WordItem item = holder.mItem;


        //텍스트뷰에 아이템 객체의 정보 세팅

        //단어명 세팅
        holder.tvNameWord.setText(item.nameWord);

        //단어 뜻 세팅
        holder.tvMeanWord.setText(item.meanWord);


        // 프로필 이미지의 유무에 따라 프로필 이미지뷰 세팅. 없으면 -> 기본 세팅
        Log.e("abc", "onBindViewHolder: " + item.UrlWordImg);

        //UrlWordImg 가 세팅이 되어있거나 로컬에서 이미지 경로를 설정한 경우
        if (item.UrlWordImg != null && !item.UrlWordImg.equals("") || item.imgPath!=null) {

            //리퀘스트 옵션에서 에러 발생시 예외처리문 추가. 에러 생김 -> 기본 프로필로 세팅해줌

            //url 이 세팅된 경우(서버에서 불러왔음)
            if(item.UrlWordImg != null && !item.UrlWordImg.equals("") ){
                RequestOptions requestOptions = new RequestOptions()
                        .error(R.drawable.ic_image);

                Glide.with(holder.mView).load(item.UrlWordImg)
                        .apply(requestOptions)
                        .into(holder.imgWord);
            }
            //로컬 경로가 설정된 경우(새로 이미지를 추가했음, 이미지 업로드 필요)
            else if(item.imgPath!=null)
            {
                RequestOptions requestOptions = new RequestOptions()
                        .error(R.drawable.ic_image);

                Glide.with(holder.mView).load(item.imgPath)
                        .apply(requestOptions)
                        .into(holder.imgWord);

            }




            holder.tvRequestImage.setVisibility(View.GONE);
        }
        // 이미지 url이 존재하지 않음 -> 디폴트 이미지 세팅, 이미지 첨부 텍스트 보이게.
        else {

            RequestOptions requestOptions = new RequestOptions()
                    .error(R.drawable.ic_image);

            Glide.with(holder.mView).load(android.R.drawable.ic_menu_report_image)
                    .apply(requestOptions)
                    .into(holder.imgWord);
            holder.tvRequestImage.setVisibility(View.VISIBLE);
        }

        // 콜백 인터페이스의 클릭을 적용함 - 이미지뷰.

        //이미지가 세팅되지 않은 아이템이라면 이미지뷰를 클릭하여 이미지를 추가할 수 있다.
        if(item.UrlWordImg.equals("")){
            //단어 이미지뷰에 클릭 리스너 세팅
            holder.imgWord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mCallback != null) {

                        //인터페이스 메소드 호출
                        // ocr 단어장 액티비티로 이미지뷰 전달. -> 사진 촬영 등으로 이미지 세팅함.
                        mCallback.onHandleSelection(position, holder);
                    }
                }
            });
        }


        // 단어 삭제 버튼 클릭 -> 삭제 의사를 확인하고 단어 삭제 진행
        holder.imgDelWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final Context context = holder.mView.getContext();

                //다이얼로그 띄우기

                //단어장 삭제를 눌렀다.

                AlertDialog.Builder deleteConfirmBuilder = new AlertDialog.Builder(context)
                        .setMessage("단어를 단어장에서 삭제하시겠습니까?")
                        .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //삭제를 클릭했다. 삭제를 진행하는 태스크를 실행한다.

                                DeleteWordTask deleteSetTask = new DeleteWordTask(context);

                                //포지션 값을 파라미터로 보냄 -> 리스트의 position 번 째 아이템을 삭제하면 ok
                                deleteSetTask.execute(position);

//                                int idWordSetInActivity;
//
//
//
//                                OCRWordSetRecyclerViewAdapter.DeleteWordInSetTask deleteWordInSetTask
//                                        = new OCRWordSetRecyclerViewAdapter.DeleteWordInSetTask(holder.mView.getContext(), idWordSetInActivity ,item.nameWord, item.idWord);
//                                deleteWordInSetTask.execute();



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




            }
        });

//
//        View.OnClickListener itemClickListener = new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                //댓글 달기 버튼 혹은 댓글 갯수를 클릭했다면 댓글 액티비티로 이동시킨다.
//
//                int clickedViewId = v.getId();
//
//
//                if(clickedViewId == holder.imgWord.getId()){
//
//                    final OCRSelectWordActivity activity = (OCRSelectWordActivity)holder.mView.getContext();
//                    Toast.makeText(holder.mView.getContext(), "img clicked", Toast.LENGTH_SHORT).show();
//
//                    DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            captureCamera(activity);
//                        }
//                    };
//                    DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            getAlbum(activity);
//                        }
//                    };
//                    DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss(activity);
//                        }
//                    };
//
//
//                    //권한 체크 생략 : 이미지 스캔을 할 때 이미 권한을 확인했음! - 오류 안나나 확인
//
//
//                    new AlertDialog.Builder(this)
//
////                .setTitle("업로드 방식 선택")
//                            .setItems(R.array.image_way, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    // The 'which' argument contains the index position
//                                    // of the selected item
//
//                                    switch (which) {
//                                        case 0:
//                                            getAlbum();
//
//                                            break;
//                                        case 1:
//                                            captureCamera();
//
//                                            break;
//
//                                    }
//                                }
//                            })
//                            .show();
//
//
//                }
//            }
//        };
//
//        //이미지 url이 존재하지 않는다면 이미지를 추가할 수 있도록 클릭리스너 설정
//        if(item.UrlWordImg.equals("")){
//            holder.imgWord.setOnClickListener(itemClickListener);
//        }


    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;

        public OCRWordSetContent.WordItem mItem;


        @BindView(R.id.tvNameWordOcr)
        TextView tvNameWord;

        @BindView(R.id.tvMeanWordOcr)
        TextView tvMeanWord;

        @BindView(R.id.imgWordOcr)
        ImageView imgWord;

        @BindView(R.id.tvRequestImage)
        TextView tvRequestImage;

        @BindView(R.id.imgDelWord)
        ImageView imgDelWord;



        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;

        }

    }

    //단어 리스트에서 단어를 지우고, 단어장 어댑터에 notify 하여, 단어 아이템을 삭제한다.
    public class DeleteWordTask extends AsyncTask<Integer, String, String> {

        private Context mContext;

        // context를 가져오는 생성자. 이를 통해 어댑터를 품은 액티비티의 함수에 접근할 수 있다.
        //현재 액티비티 : OCRSelectWordActivity
        public DeleteWordTask(Context context) {
            mContext = context;
        }

        @Override
        protected String doInBackground(Integer... integers) {

            int index = integers[0];
            OCRWordSetContent.ITEMS.remove(index);

            return null;
        }

        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            OCRSelectWordActivity activity = (OCRSelectWordActivity) mContext;

            activity.updateRecyclerView();


        }

    }


}