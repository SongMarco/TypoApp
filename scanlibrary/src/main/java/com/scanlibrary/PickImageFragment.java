package com.scanlibrary;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jhansi on 04/04/15.
 */


// 이미지를 가져오는 프래그먼트.
// 사진 / 갤러리 버튼을 세팅.
// 사진 스캔 후 사진 가져오기를 취소했을 때 수동으로 사용하게 됨.

public class PickImageFragment extends Fragment {

    private View view;
    private ImageButton cameraButton;
    private ImageButton galleryButton;
    private Uri fileUri;

    private String imgPath;

    //scanner 액티비티
    private IScanner scanner;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof IScanner)) {
            throw new ClassCastException("Activity must implement IScanner");
        }

        //스캐너 액티비티. 나중에 onBitmapSelect, onScanFinish 등의 메소드를 호출하게 된다.
        this.scanner = (IScanner) activity;
    }


    //프래그먼트 초기화
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.pick_image_fragment, null);


        //초기화 메소드 수행
        init();
        return view;
    }

    // 프래그먼트 초기화 - 카메라와 갤러리 버튼을 초기화
    private void init() {
        cameraButton = view.findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new CameraButtonClickListener());
        galleryButton = view.findViewById(R.id.selectButton);
        galleryButton.setOnClickListener(new GalleryClickListener());

        // 인텐트에서 받은 preference 세팅 여부를 확인.

        // preference 가 세팅됨 ( 카메라 or 갤러리 )
        if (isIntentPreferenceSet()) {

            // preference 에 따라 카메라 / 갤러리 액티비티 호출
            handleIntentPreference();
        } else {

            // 아무 설정이 없음. 스캔 액티비티를 닫는다.
            getActivity().finish();
        }
    }


    // 인텐트의 preference 세팅을 확인한다.
    private boolean isIntentPreferenceSet() {

        //프래그먼트의 번들에 추가된, 인텐트 설정을 가져온다.
        int preference = getArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0);

        // preference 가 0이 아니라면 그 값을 반환한다. 상수는 ScanConstants 클래스에서  참고한다.
        //    public final static int OPEN_CAMERA = 4;
        //    public final static int OPEN_MEDIA = 5;
        return preference != 0;

    }

    // 인텐트의 Preference 값에 따라 다르게 행동한다.
    // 카메라 / 갤러리로 분기한다.
    private void handleIntentPreference() {

        // 인텐트의 설정을 가져온다.
        int preference = getIntentPreference();

        // 카메라 설정이었다면
        // 카메라 사용 권한을 체크하고, 카메라 액티비티를 호출
        if (preference == ScanConstants.OPEN_CAMERA) {

            //권한을 받았는지 확인하는 리스너
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
//                    Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();

                    openCamera();
                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    Toast.makeText(getActivity(), "권한이 거부되었습니다.\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT)
                            .show();
                }


            };

            // 카메라와 저장소 쓰기 권한을 확인받는 모듈
            TedPermission.with(getActivity())
                    .setPermissionListener(permissionlistener)
                    //아래 두 줄을 사용하면 권한을 받기 전에 다이얼로그를 띄울 수 있다.
//                    .setRationaleTitle(R.string.rationale_title)
//                    .setRationaleMessage(R.string.rationale_message)
                    //권한이 거부된 경우 띄우는 다이얼로그
                    .setDeniedTitle("접근 권한이 거부되었습니다.")
                    .setDeniedMessage(
                            "앱 기능을 이용하시려면 [설정] > [권한] 에서 권한을 허용해야 합니다.")
                    .setGotoSettingButtonText("설정")

                    //요구할 권한들 : 카메라, 저장소 쓰기 권한
                    .setPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .check();

        }
        // 갤러리 설정이라면
        // 저장소 접근 권한을 체크하고, 갤러리 액티비티를 호출 -
        else if (preference == ScanConstants.OPEN_MEDIA) {
            openMediaContent();
        }
    }

    //인텐트 설정을 가져오는 메소드
    private int getIntentPreference() {
        int preference = getArguments().getInt(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
        return preference;
    }


    ///////////////////////////버튼 리스너들 : ScanFragment 를 사용하지 않으므로, 사용하지 않음 /////////////////////
    private class CameraButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            openCamera();
        }
    }

    private class GalleryClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            openMediaContent();
        }
    }

    public void openMediaContent() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("image/*");
//        startActivityForResult(intent, ScanConstants.PICKFILE_REQUEST_CODE);
        Log.i("getAlbum", "Call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, ScanConstants.PICKFILE_REQUEST_CODE);
    }


    //사진 액티비티를 호출하는 메소드
    public void openCamera() {

        // 사진 액티비티를 부르는 인텐트 생성
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        //사진을 담을 파일 생성
        File file = createImageFile();

        imgPath = file.getAbsolutePath();

        // 디렉토리가 없다면 생성함.
        // 디렉토리가 생성되면 true, 아니면 false
        boolean isDirectoryCreated = file.getParentFile().mkdirs();
        Log.d("", "openCamera: isDirectoryCreated: " + isDirectoryCreated);


        // 안드로이드 운영체제의 버전에 따라 분기.

        // FileProvider 를 이용하여 파일을 가져와야 될 경우 - 누가버전 이상은 Uri 를 fileProvider 로 가져와야 한다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Toast.makeText(getActivity(), "버전이 누가버전 이상 입니다.", Toast.LENGTH_SHORT).show();
            Uri tempFileUri = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                    "nova.typoapp", // As defined in Manifest
                    file);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri);
        }


        // 누가보다 낮은 버전은 Uri 를 그냥 가져올 수 있다.
        else {
            Toast.makeText(getActivity(), "버전이 누가보다 낮습니다..", Toast.LENGTH_SHORT).show();
            Uri tempFileUri = Uri.fromFile(file);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri);
        }

        // 카메라 액티비티 시작 -> onActivityResult 로 돌아온다.
        startActivityForResult(cameraIntent, ScanConstants.START_CAMERA_REQUEST_CODE);
    }


    //이미지를 담을 파일을 생성하는 메소드
    private File createImageFile() {

        //임시 이미지를 삭제
        clearTempImages();

        // 20160826_193422 식으로 파일명 생성. - 연월일_시분초
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new
                Date());

        //@@@@@ 주의 : 아래 파일의 경로가 파일 프로바이더의 루트 패스 이하임에 유의.
        File file = new File(ScanConstants.PATH_SCANNED_IMG, "scanImg" + timeStamp +
                ".jpg");

        //파일의 Uri 생성
        fileUri = Uri.fromFile(file);

        //파일 반환
        return file;
    }

    //임시 이미지를 삭제
    private void clearTempImages() {
        try {
            File tempFolder = new File(ScanConstants.PATH_SCANNED_IMG);

            // 스캔한 이미지 디렉토리 안에 있는 임시파일을 모두 삭제한다.
            for (File f : tempFolder.listFiles())
                f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 사진 촬영 / 갤러리에서 사진 가져오기 완료
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("", "onActivityResult" + resultCode);

        // 이미지를 담을 비트맵
        Bitmap bitmap = null;

        //정상적인 결과를 받았다면
        if (resultCode == Activity.RESULT_OK) {


            try {

                //사진 / 갤러리에 따라 분기
                switch (requestCode) {
                    case ScanConstants.START_CAMERA_REQUEST_CODE:
                        bitmap = getBitmap(fileUri);
                        break;

                    case ScanConstants.PICKFILE_REQUEST_CODE:


                        Uri photoURI = data.getData();
                        imgPath = getRealPathFromURI(getActivity(), photoURI);


                        bitmap = getBitmap(photoURI);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        //비정상 결과. 스캔 액티비티를 그냥 닫는다.
        else {
            getActivity().finish();
        }

        //비트맵이 세팅되있다면 스캔 프래그먼트로 이미지를 보내준다.
        if (bitmap != null) {
            postImagePick(bitmap);
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


    // 스캔 프래그먼트로 이미지를 보내준다.
    protected void postImagePick(Bitmap bitmap) {
        Uri uri = Utils.getUri(getActivity(), bitmap);
        bitmap.recycle();

        // 스캔 액티비티에서 onBitmapSelect 메소드 수행
        scanner.onBitmapSelect(uri, imgPath);
    }

    // Uri 에서 비트맵을 가져오는 메소드
    private Bitmap getBitmap(Uri selectedimg) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
        AssetFileDescriptor fileDescriptor = null;
        fileDescriptor =
                getActivity().getContentResolver().openAssetFileDescriptor(selectedimg, "r");
        Bitmap original
                = BitmapFactory.decodeFileDescriptor(
                fileDescriptor.getFileDescriptor(), null, options);
        return original;
    }


}