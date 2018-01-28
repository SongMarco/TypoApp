package nova.typoapp.group;

import android.Manifest;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nova.typoapp.R;
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


//그룹을 추가하는 액티비티

public class AddGroupActivity extends AppCompatActivity {



    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;




    // 그룹 이름
    @BindView(R.id.editGroupName)
    EditText editGroupName;

    // 그룹 설명
    @BindView(R.id.editGroupContent)
    EditText editGroupContent;

    @BindView(R.id.layoutSampleGroup)
    LinearLayout layoutSampleGroup;

    @BindView(R.id.imgAddGroup)
    ImageView imgAddGroup;








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        ButterKnife.bind(this);



        //툴바를 세팅한다.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarAddGroup);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }




////////// 이미지 관련 파트 ////////////


    //이미지 관련 변수들
    File albumFile;
    Uri imageUri;
    Uri photoURI, albumURI;
    String cameraPhotoPath;
    String pickPhotoPath;
    String uploadImagePath;

    // 이미지 추가 버튼을 클릭하면 발생하는 이벤트 : 사진 / 갤러리 이미지 추가하기
    @OnClick(R.id.frameAddGroupImg)
    void onAddImage() {

        //먼저 카메라와 쓰기 권한을 확인한다.

        //permissionlistener 를 이용, 권한 허용 여부를 파악한 뒤 이미지 추가로 이동한다.
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                // 아래 두 문장은 권한을 호출하기 전에 불림. 따로 세팅 안해도 ok
        .setRationaleTitle(R.string.rationale_title)
        .setRationaleMessage(R.string.rationale_message)

                //아래 세 줄은 권한이 거부되었을 때 다이얼로그를 띄우게 됨.
                //사용자가 권한을 다시 설정할 기회를 줌. 이래도 안됨 -> permissionListener 의 onPermissionDenied 로 이동함
                .setDeniedTitle("접근 권한이 거부되었습니다.")
                .setDeniedMessage(
                        "앱 기능을 이용하시려면 [설정] > [권한] 에서 권한을 허용해야 합니다.")
                .setGotoSettingButtonText("확인")


                // 설정이 필요한 권한들. - 카메라, 저장소 읽기 / 쓰기 등을 설정할 수 있음.
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

    }


    //권한의 허용 여부를 파악하는 권한 리스너. ->
    PermissionListener permissionlistener = new PermissionListener() {


        //권한이 허용되었음. - 필요한 메커니즘을 구동하면 됨(카메라 켜기, 갤러리 켜기 등)
        @Override
        public void onPermissionGranted() {

//            Toast.makeText(AddGroupActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();

            new AlertDialog.Builder(AddGroupActivity.this)

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


        //권한이 거부되었음. 더 이상 행동하지 않게 됨.
        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(AddGroupActivity.this, "카메라 관련 권한이 거부되었습니다. 앱 설정에서 권한을 변경하세요.\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT)
                    .show();
        }
    };



    private void getAlbum() {
        Log.i("getAlbum", "Call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
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
                                        Toast.makeText(AddGroupActivity.this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                                        return false;
                                    }

                                    //로딩 성공 -> 힌트용 이미지뷰를 보이지 않도록 처리
                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                        findViewById(R.id.layoutSampleGroup).setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .into(imgAddGroup);




                        Log.e("myimg", "onActivityResult: " + imgAddGroup.getDrawable().toString());

//                        iv_view.setImageURI(imageUri);

                    } catch (Exception e) {
                        Log.e("REQUEST_TAKE_PHOTO", e.toString());
                    }
                }

                // 사진 촬영을 취소한 경우
                else {
                    Toast.makeText(AddGroupActivity.this, "사진찍기를 취소하였습니다.", Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(AddGroupActivity.this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                                            return false;
                                        }

                                        //로딩 성공 -> 힌트용 이미지뷰를 보이지 않도록 처리
                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                            findViewById(R.id.layoutSampleGroup).setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(imgAddGroup);


                            Log.e("img", "real photo path: " + pickPhotoPath);


                        }

                        // 정상적으로 갤러리에서 사진을 가져오지 못함
                        catch (Exception e) {

                            Log.e("TAKE_ALBUM_SINGLE ERROR", e.toString());
                        }
                    }
                }
                break;

                //크롭 기능 사용 안함
//            case REQUEST_IMAGE_CROP:
//                if (resultCode == Activity.RESULT_OK) {
//
//                    galleryAddPic();
//
//                    imageViewAdd.setImageURI(albumURI);
//                    Log.e("img", "onActivityResult: " + albumURI.toString());
//
//
//                }
//                break;
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



///////////// 이미지 파트 끝 ////////////



    @OnClick(R.id.buttonAddGroup)
    void onWrite() {

        // 등록한 그룹 이미지가 있을 경우 / 없을 경우로 분기

        //등록한 그룹 이미지가 있을 경우 -> 이미지를 업로드하고 그룹을 등록함
        //이미지를 업로드하는 asyncTask 가 작동한 후, 그룹을 등록하게 된다.

        if (cameraPhotoPath != null || pickPhotoPath != null) {
            UploadGroumImgTask uploadTask = new UploadGroumImgTask();
            uploadTask.execute();
        }

        //등록한 그룹 이미지가 없을 경우 -> 그냥 그룹을 등록
        else {
            AddGroupTask addGroupTask = new AddGroupTask();
            addGroupTask.execute();
        }


    }



    public class UploadGroumImgTask extends AsyncTask<Void, String, String> {


        ProgressDialog asyncDialog = new ProgressDialog(
                AddGroupActivity.this);

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

            AddGroupTask addGroupTask = new AddGroupTask();
            addGroupTask.execute();


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



    //그룹을 추가하는 어싱크태스크
    String json_result = "";
    public class AddGroupTask extends AsyncTask<Void, String, String> {

        Context mContext = AddGroupActivity.this;
        @Override
        protected String doInBackground(Void... voids) {

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

            Log.e("myimg", "doInBackground: " + uploadImagePath);


            Call<ResponseBody> restCall;

            String nameGroup = editGroupName.getText().toString();
            String contentGroup = editGroupContent.getText().toString();

            restCall = apiService.addGroup( nameGroup, contentGroup, uploadImagePath);

//            //todo 수정 관련 코드 추가하기 - 신규 작성만 있음
//            if(getIntent().getStringExtra("title")!= null){
//
////                int feedID = getIntent().getIntExtra("feedID",0 );
////
////                //수정인데, 이미지를 수정했는지에 따라 분기가 갈린다.
////                if(uploadImagePath !=null){
////                    comment = apiService.editFeed(feedID, writer, email, editTitle.getText().toString(), editContent.getText().toString(), uploadImagePath);
////
////                }
////                else{
////                    comment = apiService.editFeed(feedID, writer, email, editTitle.getText().toString(), editContent.getText().toString(), getIntent().getStringExtra("imgUrl"));
////                }
//
//                //수정작업
//            }
//            //신규 작성이다
//            else{
//
//
//            }

            try {

                json_result = restCall.execute().body().string();
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
                Toast.makeText(AddGroupActivity.this, "그룹을 추가하였습니다.", Toast.LENGTH_SHORT).show();


                finish();
            }
            //글쓰기가 실패함
            //에러메시지를 확인하고, 해당 에러를 텍스트뷰에 세팅한다.
            else {

                Toast toast = Toast.makeText(AddGroupActivity.this, "그룹 추가에 실패하였습니다.", Toast.LENGTH_SHORT);
                int offsetX = 0;
                int offsetY = 0;
                toast.setGravity(Gravity.CENTER, offsetX, offsetY);
                toast.show();

            }

        }

    }



















    //뒤로가기 버튼을 눌렀을 때
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
