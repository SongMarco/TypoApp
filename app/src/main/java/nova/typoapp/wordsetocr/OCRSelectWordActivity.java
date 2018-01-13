package nova.typoapp.wordsetocr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.R;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;


/*
 AddWordSetOCRActivity (단어장 OCR 추가 액티비티)에서 가져온
 단어들로 단어장을 구성하는 액티비티.

 화면에, 문자 인식 액티비티에서 만든 리스트를 토대로 기본 단어장이 제공된다.
 사용자는 단어의 과거형, 복수형을 원형으로 변경(추후 원형을 미리 세팅하도록 구현 예정)하고,

 필요 없는 단어를 단어장에서 제외할 수 있다.


  */

public class OCRSelectWordActivity extends AppCompatActivity implements OCRWordSetRecyclerViewAdapter.CallbackInterface {


    private static final String TAG = OCRSelectWordActivity.class.getSimpleName();

    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;


    // 단어 리스트
    private ArrayList<String> listWord;

    @BindView(R.id.rvOcrWordSet)
    RecyclerView rvOcrWordSet;

    OCRWordSetRecyclerViewAdapter recyclerViewAdapter = new OCRWordSetRecyclerViewAdapter(this, OCRWordSetContent.ITEMS);

    ProgressDialog progressDialogInOcr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocrselect_word);

        ButterKnife.bind(this);


        //상단 툴바를 세팅한다.
        Toolbar toolbarComment = (Toolbar) findViewById(R.id.toolbarOcr);
        setSupportActionBar(toolbarComment);

        getSupportActionBar().setTitle("추가할 단어 선택");

        // 먼저 단어 리스트를, 단어 인식 액티비티로부터 전해 받은 인텐트에서 꺼낸다.
        listWord = getIntent().getStringArrayListExtra("listWord");

        //먼저 단어 리스트에서, 서버의 단어 DB에 세팅되지 않은 단어를 선정해야 한다. -> 이 단어들은 구글 번역 api로 단어 뜻을 검색하게 된다.

        CheckWordSetTask checkWordSetTask = new CheckWordSetTask();
        checkWordSetTask.execute();

    }

    /*
    툴바 옵션 생성

    여기에는 검은색의 체크 버튼이 있다.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ocr, menu);
        return true;
    }

    /*
    툴바 옵션 아이템을 클릭하면 취하는 행동
     */


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        Intent intent;


        //선택한 아이템에서 id를 취하여, 조건문으로 아이템에 맞게 행동한다.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_confirm:

                // 단어장 생성을 확인하였다.
                // 서버에 단어장 정보를 업로드한다.
                // 순서 : 신규 단어들 서버에 추가 -> 신규 단어장 추가.


                progressDialogInOcr = new ProgressDialog(this);
                progressDialogInOcr.setMessage("단어장을 업로드 중입니다...");
                progressDialogInOcr.show();

                Toast.makeText(this, "단어장을 업로드했습니다.", Toast.LENGTH_SHORT).show();

                UploadOcrSetTask uploadOcrSetTask = new UploadOcrSetTask();
                uploadOcrSetTask.execute();

                // 액티비티 종료 -> 메인 액티비티로 이동
                finish();
                break;

            //noinspection SimplifiableIfStatement
        }

        return super.onOptionsItemSelected(item);
    }


    //단어장 리스트의 정보를 서버에 업로드하는 태스크
    public class UploadOcrSetTask extends AsyncTask<Void, Void, String> {


        Context mContext = OCRSelectWordActivity.this;


        @Override
        protected String doInBackground(Void... params) {


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


            //단어명만 보낼 스트링 어레이를 만든다.
            ArrayList<String> arrayForSend = new ArrayList<>();

            // 스트링 어레이에 단어장 리스트의 단어명을 채운다.
            for (int i = 0; i < OCRWordSetContent.ITEMS.size(); i++) {

                String nameWord = OCRWordSetContent.ITEMS.get(i).nameWord;
                arrayForSend.add(nameWord);
            }


            // 스트링 어레이를 서버로 보낸다.
            Call<ResponseBody> comment = apiService.uploadOcrSet(arrayForSend);

            String json_result;

            try {
                json_result = comment.execute().body().string();

            } catch (IOException e) {
                e.printStackTrace();
            }

            //endregion

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialogInOcr.dismiss();

        }
    }


    //단어장 세트를 세팅하는 태스크
    public class CheckWordSetTask extends AsyncTask<Void, Void, String> {


        Context mContext = OCRSelectWordActivity.this;



        List<OCRWordSetContent.WordItem> productItems = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialogInOcr = new ProgressDialog(OCRSelectWordActivity.this);
            progressDialogInOcr.setMessage("단어장을 세팅하는 중입니다...");
            progressDialogInOcr.show();

        }

        @Override
        protected String doInBackground(Void... params) {


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

            Call<ResponseBody> comment = apiService.checkWordExistInServer(listWord);


            String json_result;


            try {
                json_result = comment.execute().body().string();
                JSONArray jsonRes = null;

                jsonRes = new JSONArray(json_result);

                for (int i = 0; i < jsonRes.length(); i++) {
                    JSONObject jObject = jsonRes.getJSONObject(i);  // JSONObject 추출

                    //단어 id. 단어 db에 존재하지 않는 단어의 경우 -1 값이 세팅됨
                    //단어 id 가 -1 일 경우 구글 번역 api 에서 단어의 뜻을 가져올 것이다.
                    int idWord = jObject.getInt("idWord");

                    //단어명(ex : good)
                    String nameWord = jObject.getString("nameWord");
                    //단어 뜻(ex : 좋은)
                    String meanWord = jObject.getString("meanWord");


                    //이미지 url
                    String imgUrl = "";
                    //이미지 url이 존재하면 담기
                    if (!Objects.equals(jObject.getString("imgUrl"), "")) {
                        imgUrl = jObject.getString("imgUrl");
                    }

                    //json 에서 받아온 데이터로 단어 아이템 객체를 새로 만든다.
                    OCRWordSetContent.WordItem productWord = new OCRWordSetContent.WordItem(idWord, imgUrl, nameWord, meanWord);

                    //생성한 아이템 객체를 아이템 리스트에 담는다. -> 단어 불러오기 작업 완료
                    productItems.add(productWord);

                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //endregion

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            //먼저 productItems 에서, 단어 뜻이 없는 단어의 뜻을 구글 번역 api로 세팅해준다

            //아래 태스크가 끝나면 리사이클러뷰를 세팅하게 된다.
            GoogleTranslatorTask googleTranslatorTask = new GoogleTranslatorTask(productItems);
            googleTranslatorTask.execute();




        }
    }


    //구글 번역을 하는 태스크

    public class GoogleTranslatorTask extends AsyncTask<Void, Void, Void> {

        private final static String URL = "https://www.googleapis.com/language/translate/v2?key=";

        private final static String KEY = "AIzaSyAdZz-QCgViUa7XDCOOfeOO9bG6XviFPcs";  // 구글 개발자 콘솔에서 발급받은 App Key

        private final static String TARGET = "&target=ko";     // 번역을 원하는 국가 코드

        private final static String SOURCE = "&source=en";   // 번역 원본의 국가 코드

        private final static String QEURY = "&q=";


        List<OCRWordSetContent.WordItem> productItems;

        // 아이템 리스트어레이를 받는 생성자.
        public GoogleTranslatorTask(List<OCRWordSetContent.WordItem> productItems) {

            this.productItems = productItems;

        }


        String englishString = "Original English String";

        String koreaString;    // 번역 후 한글을 받아올 변수


        @Override

        protected Void doInBackground(Void... params) {


            // product item의 사이즈만큼 반복작업
            for (int i = 0; i < productItems.size(); i++) {

                StringBuilder result = new StringBuilder();


                // 단어의 뜻이 세팅되어있지 않다면 번역 작업 진행
                if (productItems.get(i).meanWord.equals("")) {

                    try {
                        //단어 뜻을 encodedText 에 세팅 -> 번역될 것임
                        String encodedText = URLEncoder.encode(productItems.get(i).nameWord, "UTF-8");

                        //리퀘스트 url을 만든다.
                        URL url = new URL(URL + KEY + SOURCE + TARGET + QEURY + encodedText);


                        //http url connection 으로 api 와 연결한다.
                        HttpsURLConnection httpURLConn = (HttpsURLConnection) url.openConnection();

                        InputStream stream;

                        //정상적으로 연결이 되었다면
                        if (httpURLConn.getResponseCode() == 200) {
                            stream = httpURLConn.getInputStream();
                        }

                        //비정상적인 연결이라면
                        else {
                            //에러를 가져온다.
                            stream = httpURLConn.getErrorStream();
                        }


                        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                        String line;

                        while ((line = reader.readLine()) != null) {

                            result.append(line);

                        }


                        // result 는 json 데이터
                        JsonParser parser = new JsonParser();


                        //json parser 로 json 을 파싱
                        JsonElement element = parser.parse(result.toString());


                        //정상적으로 파싱이 진행되었다면
                        if (element.isJsonObject()) {

                            //제이슨 오브젝트를 얻어온다.
                            JsonObject obj = element.getAsJsonObject();


                            //에러없이 제이슨 오브젝트를 얻었음
                            if (obj.get("error") == null) {

                                // Json date 를 파싱하여 "translations" 하위 데이터 삽입

                                koreaString = obj.get("data").getAsJsonObject().

                                        get("translations").getAsJsonArray().

                                        get(0).getAsJsonObject().

                                        get("translatedText").getAsString();

                                // 번역된 단어 뜻을 리스트에 세팅

                                productItems.get(i).meanWord = koreaString;


                            }
                        }


                        if (httpURLConn.getResponseCode() != 200) {
                            Log.e("GoogleTranslatorTask", result.toString());
                        }
                    } catch (IOException | JsonSyntaxException ex) {
                        Log.e("GoogleTranslatorTask", ex.getMessage());
                    }

                }
            }
            return null;

        }


        @Override
        protected void onPostExecute(Void s) {
            super.onPostExecute(s);


            //기존의 단어 리스트를 비우고,
            //새로 생성한 단어 리스트를 담는다.
            OCRWordSetContent.ITEMS.clear();
            OCRWordSetContent.ITEMS.addAll(productItems);

            //리사이클러뷰에 레이아웃 매니저 세팅
            if (rvOcrWordSet.getLayoutManager() == null) {
                rvOcrWordSet.setLayoutManager(new LinearLayoutManager(OCRSelectWordActivity.this));
            }

            //단어장 리사이클러뷰에 어댑터 세팅
            rvOcrWordSet.setAdapter(recyclerViewAdapter);

            //리사이클러뷰 세팅 완료. 프로그레스 다이얼로그를 해제한다.
            progressDialogInOcr.dismiss();
        }
    }


    //사진 촬영 / 이미지 가져오기에 필요한 변수들

    Uri imageUri;
    Uri photoURI, albumURI;
    File albumFile;
    String cameraPhotoPath, pickPhotoPath;
    ImageView imageViewAdd;
    OCRWordSetContent.WordItem clickedItem;
    OCRWordSetRecyclerViewAdapter.ViewHolder clickedViewHolder;

    //리사이클러뷰의 이미지뷰를 선택함 - 새로운 방식 : 콜백 인터페이스를 적용, 액티비티와 어댑터가 소통하고 있음
    @Override
    public void onHandleSelection(int position, OCRWordSetRecyclerViewAdapter.ViewHolder viewHolder) {

        // 사진 촬영 / 갤러리 에서 가져온 이미지를 세팅하기 위한 뷰
        imageViewAdd = viewHolder.imgWord;

        //클릭한 아이템 -> 나중에 이미지 경로를 설정하게 됨
        clickedItem = OCRWordSetContent.ITEMS.get(position);

        clickedViewHolder = viewHolder;

        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                captureCamera();
            }
        };
        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getAlbum();
            }
        };
        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };

        //권한 체크 생략. 스캔할 때 이미 권한을 확인함

        new AlertDialog.Builder(this)

//                .setTitle("업로드 방식 선택")
                .setItems(R.array.image_way, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item

                        switch (which) {
                            case 0:
                                getAlbum();

                                break;
                            case 1:
                                captureCamera();

                                break;

                        }
                    }
                })
                .show();


    }


    //다이얼로그에서 사진 촬영 선택
    private void captureCamera() {

        // 외장 메모리의 상태를 가져옴
        String state = Environment.getExternalStorageState();

        // 외장 메모리의 장착 상태를 검사
        if (Environment.MEDIA_MOUNTED.equals(state)) {

            //사진 촬영을 위한 인텐트 생성
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                //촬영한 이미지를 담기 위한 파일 생성
                File photoFile = null;
                try {
                    //사진 파일 생성
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e("captureCamera Error", ex.toString());
                }
                if (photoFile != null) {
                    // getUriForFile의 두 번째 인자는 App Manifest file provider 의 authorities 와 일치해야 한다. 오타 주의

                    Uri providerURI = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                    imageUri = providerURI;

                    // 인텐트에 전달할 때는 FileProvider 의 Return 값인 content://로만!!, providerURI 의 값에 카메라 데이터를 넣어 보냄
                    // 인텐트에 엑스트라로, 촬영한 이미지가 저장될 경로를 추가해준다.
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);

                    // 사진 촬영 앱이 실행되며 촬영 시작 -> 종료시 onActivityResult 로 이동.
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } else {
            Toast.makeText(this, "저장공간이 접근 불가능한 기기입니다", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    //새 사진 파일을 생성하는 메소드.
    public File createImageFile() throws IOException {
        // 이미지 파일 이름 생성
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        File imageFile = null;

        //@@@ 주의사항 :: 아래 파일의 경로가 provider_paths.xml 의 child 여야함. 다르면 illegalArgumentException 뜬다.
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures/TypoApp", "cameraImg");

        // storageDir 디렉토리가 존재하지 않으면 새로 생성
        if (!storageDir.exists()) {
            Log.i("mCurrentPhotoPath1", storageDir.toString());
            storageDir.mkdirs();
        }

        // 이미지 파일을 저장 디렉토리에 생성
        imageFile = new File(storageDir, imageFileName);

        // 이미지의 경로를 이 파일의 경로로 지정 -> 사진 파일의 경로 지정 완료.
        cameraPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }


    private void getAlbum() {
        Log.i("getAlbum", "Call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }


    //사진을 파일로 저장함
    private void galleryAddPic() {
        Log.i("galleryAddPic", "Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        // 해당 경로에 있는 파일을 객체화(새로 파일을 만든다는 것으로 이해하면 안 됨)

        //
        File f = new File(cameraPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(this, "사진이 앨범에 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    // 카메라 전용 크랍
    public void cropImage() {
        Log.i("cropImage", "Call");
        Log.i("cropImage", "photoURI : " + photoURI + " / albumURI : " + albumURI);

        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        // 50x50픽셀미만은 편집할 수 없다는 문구 처리 + 갤러리, 포토 둘다 호환하는 방법
        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI, "image/*");
        //cropIntent.putExtra("outputX", 200); // crop한 이미지의 x축 크기, 결과물의 크기
        //cropIntent.putExtra("outputY", 200); // crop한 이미지의 y축 크기
        cropIntent.putExtra("aspectX", 1); // crop 박스의 x축 비율, 1&1이면 정사각형
        cropIntent.putExtra("aspectY", 1); // crop 박스의 y축 비율
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("output", albumURI); // 크랍된 이미지를 해당 경로에 저장
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }


    // 사진을 찍거나 갤러리로 간 후
    // 돌아왔을 때 진행하는 작업들
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // 리퀘스트 코드에 따라 분기
        switch (requestCode) {

            //사진을 찍었던 경우
            case REQUEST_TAKE_PHOTO:

                //정상적으로 사진을 촬영하였음
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Log.i("REQUEST_TAKE_PHOTO", "OK");

                        // 촬영한 사진을 파일로 저장함
                        galleryAddPic();

                        // 이미지의 경로는 사진파일이 저장된 경로다. - createImageFile() 메소드에서 생성됨
                        String imagePath = cameraPhotoPath;

                        //아이템의 이미지 경로를 수정함 -> 온바인드에서 불려도 이미지가 세팅됨
                        clickedItem.imgPath = imagePath;


                        Glide.with(this).load(imagePath)

                                //글라이드에서 이미지 로딩이 완료될 경우 발생하는 이벤트.
                                .listener(new RequestListener<Drawable>() {

                                    //로딩 실패
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        Toast.makeText(OCRSelectWordActivity.this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }

                                    //로딩 성공 -> 힌트용 이미지뷰를 보이지 않도록 처리
                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

//                                        findViewById(R.id.layoutAddImage).setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .into(imageViewAdd);

                        clickedViewHolder.tvRequestImage.setVisibility(View.GONE);


                        Log.e("myimg", "onActivityResult: " + imageViewAdd.getDrawable().toString());

//                        iv_view.setImageURI(imageUri);

                    } catch (Exception e) {
                        Log.e("REQUEST_TAKE_PHOTO", e.toString());
                    }
                }

                // 사진 촬영을 취소한 경우
                else {
                    Toast.makeText(OCRSelectWordActivity.this, "사진찍기를 취소하였습니다.", Toast.LENGTH_SHORT).show();
                }
                break;


            //갤러리에서 사진을 가져오려 한 경우
            case REQUEST_TAKE_ALBUM:

                //정상적으로 사진을 가져왔을 경우
                if (resultCode == Activity.RESULT_OK) {

                    if (data.getData() != null) {
                        try {
                            albumFile = null;
                            albumFile = createImageFile();
                            photoURI = data.getData();
                            albumURI = Uri.fromFile(albumFile);

                            //이미지 크롭을 하지 않으므로 주석처리
//                            cropImage();


                            //그냥 getPath하면 작동하지 않으나 해당 함수를 사용 하면 작동한다@@@
                            pickPhotoPath = getRealPathFromURI(this, photoURI);

                            clickedItem.imgPath = pickPhotoPath;

                            Glide.with(this).load(pickPhotoPath)
                                    //글라이드에서 이미지 로딩이 완료될 경우 발생하는 이벤트.
                                    .listener(new RequestListener<Drawable>() {

                                        //로딩 실패
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            Toast.makeText(OCRSelectWordActivity.this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                                            return false;
                                        }

                                        //로딩 성공 -> 힌트용 이미지뷰를 보이지 않도록 처리
                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

//                                            findViewById(R.id.layoutAddImage).setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(imageViewAdd);

                            clickedViewHolder.tvRequestImage.setVisibility(View.GONE);


                            Log.e("img", "real photo path: " + pickPhotoPath);


                        }

                        // 정상적으로 갤러리에서 사진을 가져오지 못함
                        catch (Exception e) {

                            Log.e("TAKE_ALBUM_SINGLE ERROR", e.toString());
                        }
                    }
                }
                break;

            case REQUEST_IMAGE_CROP:
                if (resultCode == Activity.RESULT_OK) {

                    galleryAddPic();

                    imageViewAdd.setImageURI(albumURI);
                    Log.e("img", "onActivityResult: " + albumURI.toString());


                }
                break;
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

    public void updateRecyclerView() {

        recyclerViewAdapter.notifyDataSetChanged();
    }


}
