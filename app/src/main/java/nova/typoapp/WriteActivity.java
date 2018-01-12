package nova.typoapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nova.typoapp.newsfeed.NewsFeedContent;
import nova.typoapp.permission.PermissionsActivity;
import nova.typoapp.permission.PermissionsChecker;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ImageUploadResult;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import nova.typoapp.retrofit.RetroClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;

public class WriteActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;

    /**
     * Permission List
     */
    private static final String[] PERMISSIONS_READ_STORAGE = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

    @BindView(R.id.editTitle)
    EditText editTitle;
    @BindView(R.id.editContent)
    EditText editContent;

    @BindView(R.id.layoutAddImage)
    LinearLayout layoutAddImage;
    @BindView(R.id.imageViewAdd)
    ImageView imageViewAdd;
    @BindView(R.id.textViewAddRequire)
    TextView textViewAdd;

    File albumFile;
    Uri imageUri;
    Uri photoURI, albumURI;
    String cameraPhotoPath;
    String pickPhotoPath;
    String uploadImagePath;
    private String absolutePath;
    ProgressDialog progressDialog;
    String writer, email;

    PermissionsChecker checker;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        ButterKnife.bind(this);


//        //키보드의 언어 설정 - 영어단어는 영어, 단어 뜻은 한글로
//        editTitle.setPrivateImeOptions("defaultInputmode=english;");
//        editContent.setPrivateImeOptions("defaultInputmode=korean;");


        intent = getIntent();

        if (intent.getStringExtra("title") != null) {



            editTitle.setText(intent.getStringExtra("title"));
            editContent.setText(intent.getStringExtra("content"));



            if(!Objects.equals(intent.getStringExtra("imgUrl"), "")){
                Glide.with(WriteActivity.this)
                        .load(intent.getStringExtra("imgUrl"))
                        .into(imageViewAdd);
                findViewById(R.id.layoutAddImage).setVisibility(View.INVISIBLE);
            }


        }

        /**
         * Permission Checker Initialized
         */
        checker = new PermissionsChecker(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        SharedPreferences pref_login = getSharedPreferences(getString(R.string.key_pref_Login), Activity.MODE_PRIVATE);

        writer = pref_login.getString(getString(R.string.cookie_name), "");
        email = pref_login.getString("cookie_email", "");

    }


    @OnClick(R.id.frameAdd)
    void onAddImage() {
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

        checkPermission();

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

//        .   show();
//                .setPositiveButton("사진촬영", cameraListener)
//                .setNeutralButton("앨범선택", albumListener)
//                .setNegativeButton("취소", cancelListener)


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
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "jamsya");

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

        //ㅋ
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

                        Glide.with(this).load(imagePath)

                                //글라이드에서 이미지 로딩이 완료될 경우 발생하는 이벤트.
                                .listener(new RequestListener<Drawable>() {

                                    //로딩 실패
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        Toast.makeText(WriteActivity.this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }

                                    //로딩 성공 -> 힌트용 이미지뷰를 보이지 않도록 처리
                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                        findViewById(R.id.layoutAddImage).setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .into(imageViewAdd);




                        Log.e("myimg", "onActivityResult: " + imageViewAdd.getDrawable().toString());

//                        iv_view.setImageURI(imageUri);

                    } catch (Exception e) {
                        Log.e("REQUEST_TAKE_PHOTO", e.toString());
                    }
                }

                // 사진 촬영을 취소한 경우
                else {
                    Toast.makeText(WriteActivity.this, "사진찍기를 취소하였습니다.", Toast.LENGTH_SHORT).show();
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


                            Glide.with(this).load(pickPhotoPath)
                                    //글라이드에서 이미지 로딩이 완료될 경우 발생하는 이벤트.
                                    .listener(new RequestListener<Drawable>() {

                                        //로딩 실패
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            Toast.makeText(WriteActivity.this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                                            return false;
                                        }

                                        //로딩 성공 -> 힌트용 이미지뷰를 보이지 않도록 처리
                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                            findViewById(R.id.layoutAddImage).setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(imageViewAdd);


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

    /**
     * 이미지를 회전시킵니다.
     *
     * @param bitmap  비트맵 이미지
     * @param degrees 회전 각도
     * @return 회전된 이미지
     */
    //사진을 회전각도에따라 회전시켜 비트맵을 세팅하는 메소드
    public Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if (bitmap != converted) {
                    bitmap.recycle();
                    bitmap = converted;
                }
            } catch (OutOfMemoryError ex) {
                //OOM에 걸릴 경우, 비트맵을 축소하여 재생성하고, 함수에 다시 넣어준다.
//                Toast.makeText(this, "메모리 부족으로 리사이징함", Toast.LENGTH_SHORT).show();
                Log.e("img", "err: mem 부족");
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 4, bitmap.getHeight() / 4, true);
                return rotate(bitmap, degrees);

                // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
            }
        }
        return bitmap;
    }

    //회전 각도를 얻는 메소드
    public int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    //권한을 체크하는 메소드
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 처음 호출시엔 if()안의 부분은 false로 리턴 됨 -> else{..}의 요청으로 넘어감
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) ||
                    (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))) {
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_CAMERA:
                for (int i = 0; i < grantResults.length; i++) {
                    // grantResults[] : 허용된 권한은 0, 거부한 권한은 -1
                    if (grantResults[i] < 0) {
                        Toast.makeText(WriteActivity.this, "해당 권한을 활성화 하셔야 합니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // 허용했다면 이 부분에서..

                break;
        }
    }


    @OnClick(R.id.buttonWrite)
    void onWrite() {

        if (checker.lacksPermissions(PERMISSIONS_READ_STORAGE)) {
            startPermissionsActivity(PERMISSIONS_READ_STORAGE);
        }


        // 작성 / 수정시 작업이 다르다.
        //WriteTask를 수정에 맞게 수정해야 한다.
        if (cameraPhotoPath != null || pickPhotoPath != null) {

            UploadTask uploadTask = new UploadTask();
            uploadTask.execute();
        } else {
            WriteTask writeTask = new WriteTask();
            writeTask.execute();
        }


    }

    private void startPermissionsActivity(String[] permission) {
        PermissionsActivity.startActivityForResult(this, 0, permission);
    }

    String json_result = "";

    public class WriteTask extends AsyncTask<Void, String, String> {


        @Override
        protected String doInBackground(Void... voids) {


            //region//글쓰기

            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(new ReceivedCookiesInterceptor(WriteActivity.this))
                    .addInterceptor(new AddCookiesInterceptor(WriteActivity.this))
                    .addInterceptor(httpLoggingInterceptor)
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(okHttpClient)
                    .build();
            ApiService apiService = retrofit.create(ApiService.class);

            Log.e("myimg", "doInBackground: " + uploadImagePath);



            Call<ResponseBody> comment;
            //수정이라면
            if(getIntent().getStringExtra("title")!= null){

                int feedID = getIntent().getIntExtra("feedID",0 );

                //수정인데, 이미지를 수정했는지에 따라 분기가 갈린다.
                if(uploadImagePath !=null){
                    comment = apiService.editFeed(feedID, writer, email, editTitle.getText().toString(), editContent.getText().toString(), uploadImagePath);

                }
                else{
                    comment = apiService.editFeed(feedID, writer, email, editTitle.getText().toString(), editContent.getText().toString(), getIntent().getStringExtra("imgUrl"));
                }

                //수정작업
            }
            //신규 작성이다
            else{
               comment = apiService.write(writer, email, editTitle.getText().toString(), editContent.getText().toString(), uploadImagePath);
            }



            try {

                json_result = comment.execute().body().string();
                return json_result;
            } catch (IOException e) {
                e.printStackTrace();
            }


            //endregion

            return null;
        }


        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            Log.e("wow", result);


            if (result.contains("success")) {
//                Snackbar.make(findViewById(R.id.email_sign_in_button), "환영합니다. 계정"+email+"으로 가입하셨습니다.", Snackbar.LENGTH_LONG).show();


//                LauncherActivity.LoginToken = true;
                Toast.makeText(WriteActivity.this, "글을 작성하였습니다.", Toast.LENGTH_SHORT).show();


                NewsFeedContent.called = false;


                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


                finish();
            }
            //글쓰기가 실패함
            //에러메시지를 확인하고, 해당 에러를 텍스트뷰에 세팅한다.
            else {

                Toast toast = Toast.makeText(WriteActivity.this, "글 작성에 실패하였습니다.", Toast.LENGTH_SHORT);
                int offsetX = 0;
                int offsetY = 0;
                toast.setGravity(Gravity.CENTER, offsetX, offsetY);
                toast.show();

            }

        }

    }


    public class UploadTask extends AsyncTask<Void, String, String> {


        ProgressDialog asyncDialog = new ProgressDialog(
                WriteActivity.this);

        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("이미지 업로드 중입니다...");

            // show dialog
            asyncDialog.show();


            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {


            return uploadImage();
        }


        @Override

        //이미지 업로드가 종료됨. 글쓰기 태스크 시작
        protected void onPostExecute(String imgUrl) {

            super.onPostExecute(imgUrl);

            Log.e("myimg", "imgurl=" + imgUrl);

            asyncDialog.dismiss();

            WriteTask writeTask = new WriteTask();
            writeTask.execute();


        }

    }
    //이미지를 업로드하는 메소드다.
    // @@@@@주의할 점 : php에서 json만 반환하게 하라. 그렇지 않으면 오류메시지가 뜨게될 때, gsonConverter가 오류를 일으킨다! -> onFailure진입!
    // 왜? 컨버팅 하려는데 json자료형이 아니니까!


    public String uploadImage() {

        /**
         * Progressbar to Display if you need
         */


        //Create Upload Server Client
        ApiService service = RetroClient.getApiService();


        //File creating from selected URL
        File file;
        if (pickPhotoPath != null) {
            file = new File(pickPhotoPath);

        } else {
            file = new File(cameraPhotoPath);
        }
//
//        Log.e("myimg", "uploadImage-> pick"+pickPhotoPath+" ="+file.getAbsolutePath() +"is it same?");
//
//        Log.e("myimg", "uploadImage-> camera"+cameraPhotoPath+" ="+file.getAbsolutePath() +"is it same?");
        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

        Call<ImageUploadResult> resultCall = service.uploadImage(body);

        try {
            ImageUploadResult imageUploadResult = resultCall.execute().body();

            uploadImagePath = imageUploadResult.getPath();


        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.e("myimg1234", "onResponse: " + uploadImagePath);
        return uploadImagePath;


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }


}
