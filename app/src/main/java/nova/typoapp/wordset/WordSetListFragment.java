package nova.typoapp.wordset;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nova.typoapp.R;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import nova.typoapp.wordset.WordSetContent.WordSetItem;
import nova.typoapp.wordsetocr.AddWordSetOCRActivity;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;
import static nova.typoapp.wordset.WordSetContent.ITEMS;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */

/*
WordSetListFragment

1. 클래스 개요

 - 단어장 리스트 페이지를 보여주기 위한 프래그먼트

 - 단어장을 추가하거나 단어장 목록을 조회한다.

2. 클래스 구성

 1) AddWordSetTask : 단어장을 추가하기 위한 비동기 태스크
 2) GetWordSetTask : 서버에서 단어장 리스트를 불러오는 비동기 태스크
 3) 그 외 프래그먼트 기본 컴포넌트 등

3. 클래스 흐름 설명

 1) 단어장 추가하기

    a. 상단의 단어장 추가를 클릭하면 단어장 이름을 입력받는 다이얼로그를 띄운다.
    b. 다이얼로그의 editText 에 단어장 이름을 입력받는다.
    c. AddWordSetTask 가 실행되어, 서버의 단어장 DB에 해당 이름의 단어장을 추가한다.
    d. 위의 비동기 태스크가 끝나면 GetWordSetTask 가 실행되어, 서버의 단어장 목록을 가져와 화면을 업데이트한다.

 2) 단어장 목록 초기화 / 새로고침
    a. 프래그먼트가 초기화될 때(onCreateView) 에서 GetWordSetTask 가 실행된다.
    b. 위의 비동기 태스크가 실행되면 단어장 목록을 업데이트하여 화면에 보여준다.
    c. 이 흐름은 단어장을 추가할 때에도 동작한다.

 3) 단어장 조회하기
    a. 단어장 리스트의 아이템을 클릭하면 단어장 액티비티(WordSetActivity) 로 넘어간다.
    b. 단어장 액티비티에서 내부 컨텐츠를 이용할 수 있다.(학습, 퍼즐 등)

4. 특이사항
 - 단어장 리사이클러뷰의 어댑터가 세팅되지 않았다는 경고 로그가 확인되나, 실제로는 정상적으로 작동되고 있다.

 */




public class WordSetListFragment extends Fragment {


    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String TAG = "WordSetListFragment";

    //사진 스캔을 위한 리퀘스트 코드
    static final int REQUEST_CODE = 99;
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WordSetListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static WordSetListFragment newInstance(int columnCount) {
        WordSetListFragment fragment = new WordSetListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }


    // 단어장의 리스트를 보여주는 리사이클러뷰
    @BindView(R.id.rvWordSet)
    RecyclerView rvWordSet;

    // 단어장 추가하기 카드뷰.
    @BindView(R.id.cvAddWordSet)
    CardView cvAddWordSet;

    //단어장 추가하기 카드뷰에서의 + 모양 버튼 이미지
    @BindView(R.id.imgAddWordSet)
    ImageView imgAddWordSet;

    // 사진에서 단어장 추가하기 카드뷰
    @BindView(R.id.cvAddWordSetWithCam)
    CardView cvAddWordSetWithCam;


    // 단어장을 새로 추가할 때 사용되는, 새 단어장의 이름
    String nameWordSet;

    // 단어장 목록 리사이클러뷰의 어댑터
    MyWordSetItemRecyclerViewAdapter rvWordSetAdapter = new MyWordSetItemRecyclerViewAdapter(ITEMS, mListener);


    Activity wordSetActivity;

    // 프래그먼트가 생성되며 초기화됨
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wordsetitem_list, container, false);
        ButterKnife.bind(this, view);


        // + 모양 버튼을 단어장 추가하기 카드뷰에 세팅한다.
        Glide.with(getActivity()).load(R.drawable.add3).into(imgAddWordSet);

        //현재 프래그먼트를 포함한 단어장 액티비티
        wordSetActivity = getActivity();

        // 리사이클러뷰의 스크롤 기능을 막는다.
        // 리사이클러뷰를 포함한 NestedScrollView 의 스크롤을 원활하게 해주기 위함이다.
        rvWordSet.setNestedScrollingEnabled(false);


        // 사용자의 단어장 목록을 받아오는 asyncTask 를 실행한다.
        // 해당 태스크가 끝나면 단어장 목록이 화면에 세팅된다.
        GetWordSetListTask getWordSetListTask = new GetWordSetListTask();
        getWordSetListTask.execute();


        return view;
    }


    // 필요할 경우 리사이클러뷰를 갱신하는 메소드.
    // 외부 클래스(다른 액티비티, 리사이클러뷰 어댑터 등)에서 프래그먼트의 업데이트가 필요하다고 판단될 때 사용
    public void updateRecyclerView() {

        rvWordSetAdapter.notifyDataSetChanged();

    }


    // 새 단어장 추가하기를 클릭했을 때 발생하는 이벤트.
    @OnClick(R.id.cvAddWordSet)
    public void clickAddSet() {

//        Toast.makeText(getActivity(), "세트 추가 클릭", Toast.LENGTH_SHORT).show();


        //단어장 추가를 위해 필요한 다이얼로그를 세팅한다.

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("새 단어장 추가");       // 제목 설정
        //        builder.setMessage("");   // 내용 설정


        // 단어장 이름을 입력받는 에딧텍스트 삽입하기
        final EditText etNameWordSet = new EditText(getActivity());

        // 사용자에게 단어장 이름을 입력하도록 요구
        etNameWordSet.setHint(R.string.hint_etNameWordSet);


        // 단어장 입력을 위한 에딧 텍스트를 세팅하기 위해,
        // 먼저 리니어 레이아웃을 세팅
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(60, 0, 60, 0);

        // 준비된 레이아웃에 에딧 텍스트를 추가
        layout.addView(etNameWordSet, params);


        // 레이아웃을 다이얼로그에 세팅
        builder.setView(layout);

        // 확인 버튼 설정 : 확인을 누르면 새 단어장이 추가된다.
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Log.v(TAG, "Yes Btn Click"); 테스트용 로그


                // 에딧텍스트에 제목 입력을 했다면 서버로 입력 정보를 보내고, DB에 단어장 정보를 저장한다.
                if (etNameWordSet.getText() != null && !etNameWordSet.getText().toString().equals("")) {
                    nameWordSet = etNameWordSet.getText().toString();
                    Log.v(TAG, "nameWordSet = " + nameWordSet);


                    // 서버로 입력 정보를 보내는 asyncTask 를 실행
                    AddWordSetTask addWordSetTask = new AddWordSetTask();
                    addWordSetTask.execute();


                }
                // 에딧 텍스트에 단어장 이름을 입력하지 않았다면 에러 메시지를 출력하여 예외처리
                else {
                    Toast.makeText(getActivity(), "에러 : 단어장 이름을 입력하지 않으셨네요!", Toast.LENGTH_SHORT).show();
                }

                //쓸데없는 코드 같아 주석처리 해둠. 테스트시 에러가 없을 경우 삭제할 것
//                nameWordSet = etNameWordSet.getText().toString();


                //필요한 동작을 완료했으므로 다이얼로그를 닫는다.
                dialog.dismiss();

            }
        });

// 중립 버튼 설정 : 사용자가 단어장 생성을 취소할 때 누르게 되며, 다이얼로그를 그냥 닫는다.
        builder.setNeutralButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });

        //NegativeButton 을 사용하지 않으므로 주석처리. 삭제해도 무방함
//// 취소 버튼 설정
//        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
////                Log.v(TAG,"No Btn Click");
//                dialog.dismiss();     //닫기
//                // Event
//            }
//        });


        //다이얼로그 설정 완료. 화면에 다이얼로그를 띄운다.
        builder.show();


    }




    /*

    사진으로 단어장 추가하기 파트

     */

    // 사진으로 단어장 추가하기 버튼을 클릭했다.
    // 카메라, 갤러리로부터 이미지를 받아올 수 있으며,
    // 받아온 이미지의 문자를 인식하여 단어장을 생성한다.
    @OnClick(R.id.cvAddWordSetWithCam)
    public void clickAddSetWithCam() {


        //먼저 사용자가 카메라 권한이 있는지 확인한다.
        checkPermission();


        new android.app.AlertDialog.Builder(wordSetActivity)

//                .setTitle("업로드 방식 선택")
                .setItems(R.array.image_way, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item

                        Intent intent;

                        switch (which) {
                            case 0:


                                //갤러리 액티비티로 넘어간다.
                                //갤러리에서 사진을 가져와서 적당히 잘라 스캔하게 된다.

                                intent = new Intent(getActivity() , ScanActivity.class);

                                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_MEDIA);

                                startActivityForResult(intent, REQUEST_CODE);

                                break;
                            case 1:

                                //사진 촬영 액티비티로 넘어간다.
                                //사진을 촬영하면 그 사진을 적당히 잘라 스캔하게 된다.
                                intent = new Intent(getActivity() , ScanActivity.class);

                                intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, ScanConstants.OPEN_CAMERA);

                               startActivityForResult(intent, REQUEST_CODE);

                                break;

                        }
                    }
                })
                .show();

//        .   show();
//                .setPositiveButton("사진촬영", cameraListener)
//                .setNeutralButton("앨범선택", albumListener)
//                .setNegativeButton("취소", cancelListener)


    }







    //권한을 체크하는 메소드


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(wordSetActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 처음 호출시엔 if()안의 부분은 false로 리턴 됨 -> else{..}의 요청으로 넘어감
            if ((ActivityCompat.shouldShowRequestPermissionRationale(wordSetActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) ||
                    (ActivityCompat.shouldShowRequestPermissionRationale(wordSetActivity, Manifest.permission.CAMERA))) {
                new android.app.AlertDialog.Builder(wordSetActivity)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getActivity().finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(wordSetActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
            }
        }
    }

    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;


    // 카메라와 이미지에 관련된 변수들
    Uri imageUri;
    String cameraPhotoPath;
    File albumFile;
    Uri photoURI, albumURI;
    String pickPhotoPath;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_CAMERA:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {
                        Toast.makeText(wordSetActivity, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // 허용했다면 이 부분에서..

                break;
        }
    }

    private void getAlbum() {
        Log.i("getAlbum", "Call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }





    private void captureCamera() {
        String state = Environment.getExternalStorageState();
        // 외장 메모리 검사
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(wordSetActivity.getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e("captureCamera Error", ex.toString());
                }
                if (photoFile != null) {
                    // getUriForFile의 두 번째 인자는 Manifest provier의 authorites와 일치해야 함

                    Uri providerURI = FileProvider.getUriForFile(wordSetActivity, wordSetActivity.getPackageName(), photoFile);
                    imageUri = providerURI;

                    // 인텐트에 전달할 때는 FileProvier의 Return값인 content://로만!!, providerURI의 값에 카메라 데이터를 넣어 보냄
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);

                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } else {
            Toast.makeText(wordSetActivity, "저장공간이 접근 불가능한 기기입니다", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "jamsya");

        if (!storageDir.exists()) {
            Log.i("mCurrentPhotoPath1", storageDir.toString());
            storageDir.mkdirs();
        }

        imageFile = new File(storageDir, imageFileName);
        cameraPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    private void galleryAddPic() {
        Log.i("galleryAddPic", "Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        // 해당 경로에 있는 파일을 객체화(새로 파일을 만든다는 것으로 이해하면 안 됨)
        File f = new File(cameraPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
        Toast.makeText(getActivity(), "사진이 앨범에 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }


    // 카메라 전용 크랍
    public void cropImage(Uri photoURI, Uri albumURI) {
        Log.i("cropImage", "Call");
        Log.i("cropImage", "photoURI : " + photoURI + " / albumURI : " + albumURI);

        // 크롭을 위한 인텐트 생성
        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        // 50x50픽셀미만은 편집할 수 없다는 문구 처리 + 갤러리, 포토 둘다 호환하는 방법
        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI, "image/*");
//        cropIntent.putExtra("outputX", 500); // crop한 이미지의 x축 크기, 결과물의 크기
//        cropIntent.putExtra("outputY", 500); // crop한 이미지의 y축 크기
//        cropIntent.putExtra("aspectX", 4); // crop 박스의 x축 비율, 1&1이면 정사각형
//        cropIntent.putExtra("aspectY", 3); // crop 박스의 y축 비율
//        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("output", albumURI); // 크랍된 이미지를 해당 경로에 저장
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }



    // 사진을 찍거나 갤러리로 간 후
    // 돌아왔을 때 진행하는 작업들

    // 스캔 라이브러리 액티비티에서 돌아왔다.
    // 반듯해진 사진을 이미지 뷰에 세팅하여 작업을 마치게 된다.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 리퀘스트 코드, 액티비티에서 반환한 결과값이 정상이면 이미지뷰 세팅
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {


            // uri 를 번들에서 가져온다.
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);

            // 문자 인식 액티비티로 이동하여, 문자 인식을 준비한다.
            Intent intent = new Intent(getActivity() , AddWordSetOCRActivity.class);

            intent.putExtra("imgUri", uri.toString());

            startActivity(intent);

        }

    }


    //getPath로 얻어지지 않는 진짜 파일의 패스를 얻어오는 메소드
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

///////////////////////////////////////// 카메라로 단어장 추가 파트 end


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(WordSetItem item);
    }

    public class GetWordSetListTask extends AsyncTask<Void, String, String> {

        String json_result;
        private Context mContext = getContext();

        List<WordSetItem> productItems = new ArrayList<>();

        @Override
        protected String doInBackground(Void... integers) {

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

            Call<ResponseBody> comment = apiService.getWordSetList();

            try {
                json_result = comment.execute().body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }


            JSONArray jsonRes = null;
            try {

                //받아온 결과값을 jsonArray 로 만든다.
                jsonRes = new JSONArray(json_result);


                //jsonArray 에 담긴 아이템 정보들을 빼내어, 댓글 아이템으로 만들고, 리스트에 추가한다.
                for (int i = 0; i < jsonRes.length(); i++) {

                    //jsonArray 의 데이터를 댓글 아이템 객체에 담는다.
                    JSONObject jObject = jsonRes.getJSONObject(i);  // JSONObject 추출

                    int idWordSet = jObject.getInt("id_wordset");

                    String nameSet = jObject.getString("set_name");

                    String emailSetOwner = jObject.getString("email_set_owner");

                    String nameSetOwner = jObject.getString("name_set_owner");

                    int numSetWords = jObject.getInt("num_set_words");

                    int numSetTaken = jObject.getInt("num_set_taken");

                    int numSetLike = jObject.getInt("num_set_like");


                    String dateSetMade = jObject.getString("date_set_made");


                    String profileUrl = "";
                    if (!jObject.getString("profile_url").equals("")) {
                        profileUrl = jObject.getString("profile_url");
                    }


                    //아이템 객체에 데이터를 다 담은 후, 아이템을 리스트에 추가한다.


                    Log.e(TAG, "doInBackground: " + nameSet + numSetWords + nameSetOwner);
                    WordSetContent.WordSetItem productWordSetItem = new WordSetContent.WordSetItem(idWordSet, nameSet, numSetWords, nameSetOwner, profileUrl);

                    productItems.add(productWordSetItem);

//                    for(int j = 0; j<ITEMS.size(); j++){
//
//                        Log.e(TAG, "onPostExecute: "+productItems.get(i).nameWordSetOwner);
//
//                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);


            //기존의 리스트를 클리어, 리스트를 업데이트한다.
            ITEMS.clear();
            ITEMS.addAll(productItems);


            for (int j = 0; j < ITEMS.size(); j++) {

                WordSetItem item = ITEMS.get(j);
                Log.e(TAG, "onPostExecute: " + item.nameWordSetOwner + item.numWords + item.nameWordSet);

            }

            rvWordSet.setLayoutManager(new LinearLayoutManager(getContext()));

            rvWordSet.setAdapter(rvWordSetAdapter);
            rvWordSetAdapter.notifyDataSetChanged();


        }

    }


    //서버에서 단어장을 추가하도록 하는 태스크
    public class AddWordSetTask extends AsyncTask<Integer, String, String> {

        String json_result;
        private Context mContext = getContext();


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


            // 레트로핏 콜 객체를 만든다. 파라미터로 게시물의 ID값, 게시물의 타입을 전송한다.

            Call<ResponseBody> comment = apiService.addWordSet(nameWordSet);

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

            //서버로 데이터 전송이 완료되었다.
            //단어장 데이터를 갱신한다.

            GetWordSetListTask getWordSetListTask = new GetWordSetListTask();
            getWordSetListTask.execute();


        }

    }

    public void updateWordSet() {
        GetWordSetListTask getWordSetListTask = new GetWordSetListTask();
        getWordSetListTask.execute();
    }


}
