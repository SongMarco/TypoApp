package com.scanlibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ComponentCallbacks2;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by jhansi on 28/03/15.
 */

/*

 이미지 스캔을 위한 전체 액티비티. 프래그먼트가 계속 바뀌면서 스캔을 진행하게 됨

  먼저 PickImageFragment 를 가져와서, 사진촬영과 갤러리를 통해 이미지를 가져오게 된다.

  onBitmapSelected 에서 비트맵으로 꺼낼 이미지가 정해지면, ScanFragment 로 프래그먼트를 바꾼다.

  ScanFragment 에서는 이미지를 반듯하게 변형하는 작업이 수행된다.(openCv Native Method 가 사용됨)


  */


public class ScanActivity extends Activity implements IScanner, ComponentCallbacks2 {


    //액티비티 초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_layout);


        init();
    }


    // 스캔 액티비티에 PickImageFragment 세팅하는 메소드
    private void init() {

        //이미지를 가져오는 PickImageFragment 세팅
        PickImageFragment fragment = new PickImageFragment();
        Bundle bundle = new Bundle();

        // 메인 액티비티에서 보내온 리퀘스트 설정 변수를 파악한다.
        // 카메라 스캔인지, 갤러리 스캔인지를 판단하게 된다. - PickImageFragment 의 getArgument 에서 꺼내게 됨
        bundle.putInt(ScanConstants.OPEN_INTENT_PREFERENCE, getPreferenceContent());
        fragment.setArguments(bundle);

        //PickImageFragment 트랜잭션 진행, 액티비티에 PickImageFragment 세팅
        android.app.FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        // PickImageFragment 가 액티비티에 세팅됨
        fragmentTransaction.add(R.id.content, fragment);
        fragmentTransaction.commit();
    }

    // 리퀘스트 설정을 보는 메소드. 카메라 스캔 / 갤러리 스캔인지 판단
    protected int getPreferenceContent() {
        return getIntent().getIntExtra(ScanConstants.OPEN_INTENT_PREFERENCE, 0);
    }


    // 스캐너 액티비티는 Iscanner 인터페이스를 implement 하므로, 아래 2개 메소드를 수행

    // PickImageFragment 에서 비트맵 세팅이 완료되었을 때 -> ScanFragment 로 프래그먼트를 변경한다.
    @Override
    public void onBitmapSelect(Uri uri, String imgPath) {

        // ScanFragment
        ScanFragment fragment = new ScanFragment();


        // 번들로 세팅할 비트맵의 uri 저장
        Bundle bundle = new Bundle();
        bundle.putParcelable(ScanConstants.SELECTED_BITMAP, uri);
        bundle.putString("imgPath", imgPath);
        fragment.setArguments(bundle);

        //프래그먼트 트랜잭션 수행 -> ScanFragment 가 화면에 세팅됨
        android.app.FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content, fragment);
        fragmentTransaction.addToBackStack(ScanFragment.class.toString());
        fragmentTransaction.commit();
    }


    //이미지 스캔 완료.
    // 결과 프래그먼트를 액티비티에 세팅한다.
    @Override
    public void onScanFinish(Uri uri) {
        //결과 프래그먼트 초기화
        ResultFragment fragment = new ResultFragment();

        //번들로 스캔 완료한 이미지 uri 첨부
        Bundle bundle = new Bundle();
        bundle.putParcelable(ScanConstants.SCANNED_RESULT, uri);
        fragment.setArguments(bundle);

        //프래그먼트 트랜잭션 수행
        android.app.FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.content, fragment);
        fragmentTransaction.addToBackStack(ResultFragment.class.toString());
        fragmentTransaction.commit();
    }
    ///////////////////////////////////////


    @Override
    public void onTrimMemory(int level) {
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                /*
                   Release any UI objects that currently hold memory.

                   The user interface has moved to the background.
                */
                break;
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */
                break;
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */
                new AlertDialog.Builder(this)
                        .setTitle(R.string.low_memory)
                        .setMessage(R.string.low_memory_message)
                        .create()
                        .show();
                break;
            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
    }

    // 에러가 나지만, 아래 함수들은 링크가 된다. IDE에서 감지를 못할 뿐


    public native Bitmap getScannedBitmap(Bitmap bitmap, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4);

    public native Bitmap getGrayBitmap(Bitmap bitmap);

    public native Bitmap getMagicColorBitmap(Bitmap bitmap);

    public native Bitmap getBWBitmap(Bitmap bitmap);

    public native float[] getPoints(Bitmap bitmap);

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("Scanner");
    }


}