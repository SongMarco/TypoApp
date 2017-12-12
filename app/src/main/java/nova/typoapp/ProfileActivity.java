package nova.typoapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ImageUploadResult;
import nova.typoapp.retrofit.LoginInfo;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import nova.typoapp.retrofit.RetroClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;

    File albumFile;
    Uri imageUri;
    Uri photoURI, albumURI;
    String cameraPhotoPath;
    String pickPhotoPath;
    String uploadImagePath;


    @BindView(R.id.profileEmail)
    TextView profileEmail;
    @BindView(R.id.profileName)
    TextView profileName;
    @BindView(R.id.profileBirthday)
    TextView profileBirthday;
    @BindView(R.id.ImageViewProfile)
    ImageView imageViewProfile;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LookupSessionTask profileTask = new LookupSessionTask();
        profileTask.execute();





        /////////////////////////////////////////////////////////////////////////



        //,프로필


    }

    String json_result = "";

    String email, name, birthday, profileImageUrl, profileImageName;
    public class LookupSessionTask extends AsyncTask<Void, String, Void> {


        private static final String TAG = "myTag";

        @Override
        protected Void doInBackground(Void... voids) {



//            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new ReceivedCookiesInterceptor(ProfileActivity.this))
                    .addInterceptor(new AddCookiesInterceptor(ProfileActivity.this))
//                    .addInterceptor(interceptor)
                    .addNetworkInterceptor(new StethoInterceptor())
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiService.API_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();


//            Log.e(TAG, "shared-before call: "+getSharedPreferences("pref_login" , MODE_PRIVATE ).getAll()  ) ;

            ApiService apiService = retrofit.create(ApiService.class);

            Call<LoginInfo> call = apiService.lookSession();

            try {

                LoginInfo loginInfo = call.execute().body();

//                Log.e(TAG, "shared-after call: "+getSharedPreferences("pref_login" , MODE_PRIVATE ).getAll()  ) ;
//                String cookie = call.clone().execute().headers().values("Set-Cookie").toString();



                email = loginInfo.getEmail();
                name = loginInfo.getName();
                birthday = loginInfo.getBirthday();
                profileImageUrl = loginInfo.getProfile_url();

                String homeUrl = "http://115.68.231.13/project/android/profileimage/";
                String[] piecesOfHomeUrl = profileImageUrl.split("/");
//                Log.e(TAG, Arrays.toString(piecesOfHomeUrl)  );
                profileImageName = piecesOfHomeUrl[6];
//                Log.e(TAG, "doInBackground: name = "+profileImageName );



            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {

            super.onPostExecute(voids);


            if ( email!=null ) {

                RequestOptions requestOptions = new RequestOptions()
                       
                        .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));

                Glide.with(ProfileActivity.this)
                        .load(profileImageUrl)
//                        .apply(requestOptions)
                        .into(imageViewProfile);

                Log.e("myimg", "onPostExecute: "+profileImageUrl );
                profileEmail.setText(email);
                profileName.setText(name);
                profileBirthday.setText(birthday);
            }


            //아이디 중복으로 가입이 실패하였다.
            //에러메시지를 확인하고, 해당 에러를 텍스트뷰에 세팅한다.
            else {

                Toast toast = Toast.makeText(ProfileActivity.this, "로그인 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT);
                int offsetX = 0;
                int offsetY = 0;
                toast.setGravity(Gravity.CENTER, offsetX, offsetY);
                toast.show();

            }


        }

    }

    @OnClick(R.id.CardViewProfile)
    void onChangeProfileImage(){

//        checkPermission();

        new AlertDialog.Builder(this)

//                .setTitle("업로드 방식 선택")
                .setItems(R.array.image_way, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item

                        switch(which){
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



    private void captureCamera(){
        String state = Environment.getExternalStorageState();
        // 외장 메모리 검사
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e("captureCamera Error", ex.toString());
                }
                if (photoFile != null) {
                    // getUriForFile의 두 번째 인자는 Manifest provier의 authorites와 일치해야 함

                    Uri providerURI = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                    imageUri = providerURI;

                    // 인텐트에 전달할 때는 FileProvier의 Return값인 content://로만!!, providerURI의 값에 카메라 데이터를 넣어 보냄
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);

                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } else {
            Toast.makeText(this, "저장공간이 접근 불가능한 기기입니다", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {

                        Log.i("REQUEST_TAKE_PHOTO", "OK");
                        galleryAddPic();

                        String imagePath = cameraPhotoPath;


                    UploadTask uploadTask = new UploadTask();
                    uploadTask.execute();

//                        Glide.with(ProfileActivity.this).load(imageUri).into(imageViewProfile);




//                        Log.e("myimg", "onActivityResult: "+imageViewProfile.getDrawable().toString() );

//                        iv_view.setImageURI(imageUri);


                } else {
                    Toast.makeText(ProfileActivity.this, "사진찍기를 취소하였습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_TAKE_ALBUM:
                if (resultCode == Activity.RESULT_OK) {

                    if(data.getData() != null){
                        try {
                            albumFile = null;
                            albumFile = createImageFile();
                            photoURI = data.getData();
                            albumURI = Uri.fromFile(albumFile);

                            //todo 수정할 때 cropimage를 호출하도록 수정할 예정이다.
                            //todo write 하러 갈 때 액티비티를 정리하는 것이 필요함.
//                            cropImage();

                            //그냥 getPath하면 작동하지 않으나 해당 함수를 사용 하면 작동한다@@@
                            pickPhotoPath = getRealPathFromURI(this, photoURI);


                            Log.e("myimg", "onActivityResult: pickphoto::: "+pickPhotoPath );

                            UploadTask uploadTask = new UploadTask();
                            uploadTask.execute();

//                            Glide.with(ProfileActivity.this).load(photoURI).into(imageViewProfile);



                        }catch (Exception e){

                            Log.e("TAKE_ALBUM_SINGLE ERROR", e.toString());
                        }
                    }
                }
                break;

            case REQUEST_IMAGE_CROP:
                if (resultCode == Activity.RESULT_OK) {

                    galleryAddPic();

//                    imageViewAdd.setImageURI(albumURI);
                    Log.e("img", "onActivityResult: "+albumURI.toString() );




                }
                break;
        }
    }
    private void getAlbum(){
        Log.i("getAlbum", "Call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }

    private void galleryAddPic(){
        Log.i("galleryAddPic", "Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        // 해당 경로에 있는 파일을 객체화(새로 파일을 만든다는 것으로 이해하면 안 됨)
        File f = new File(cameraPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(this, "사진이 앨범에 저장되었습니다.", Toast.LENGTH_SHORT).show();
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

    //getPath로 얻어지지 않는 진짜 파일의 패스를 얻어오는 메소드
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
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




    public class UploadTask extends AsyncTask<Void, String, String> {

        ProgressDialog asyncDialog = new ProgressDialog(
                ProfileActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("이미지 업로드 중입니다...");

            // show dialog
            asyncDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
                return uploadImageProfile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override

        //이미지 업로드가 종료됨. 글쓰기 태스크 시작
        protected void onPostExecute(String imgUrl){

            super.onPostExecute(imgUrl);

            RequestOptions requestOptions = new RequestOptions()
                    .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));

//            Glide.with(ProfileActivity.this)
//                    .load(profileImageUrl)
//                    .apply(requestOptions)
//                    .into(imageViewProfile);

            LookupSessionTask lookupSessionTask = new LookupSessionTask();
            lookupSessionTask.execute();


            asyncDialog.dismiss();
            Toast.makeText(ProfileActivity.this, "프로필 사진이 수정되었습니다.", Toast.LENGTH_SHORT).show();


            Log.e("myimg", "imgurl="+imgUrl);
        }

    }


    public String uploadImageProfile() throws IOException {

        /**
         * Progressbar to Display if you need
         */


        //Create Upload Server Client
        ApiService service = RetroClient.getApiService2(ProfileActivity.this);


        //File creating from selected URL
        File file;
        if(pickPhotoPath != null){
            file = new File( pickPhotoPath  );
        }
        else{
            file = new File( cameraPhotoPath  );
        }
//
//        Log.e("myimg", "uploadImage-> pick"+pickPhotoPath+" ="+file.getAbsolutePath() +"is it same?");
//
//        Log.e("myimg", "uploadImage-> camera"+cameraPhotoPath+" ="+file.getAbsolutePath() +"is it same?");
        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("uploaded_file", profileImageUrl, requestFile);

        Call<ImageUploadResult> resultCall = service.uploadImageProfile(body);


            ImageUploadResult imageUploadResult = resultCall.execute().body();

            uploadImagePath = imageUploadResult.getPath();

            return uploadImagePath;






    }



}

