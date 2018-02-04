package com.scanlibrary;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jhansi on 29/03/15.
 */

/*
ScanFragment

PickImageFragment 에서 사진 촬영 / 갤러리를 이용해
이미지를 가져오는 작업이 끝났고,

이미지의 uri를 얻어냈다.

본 프래그먼트에서는 이미지를 비트맵으로 만들고,
삐뚤어지거나 기울어진 부분을 평평하게 만드는 작업이 진행된다.

 */


public class ScanFragment extends Fragment {

    private Button scanButton;
    private ImageView sourceImageView;
    private FrameLayout sourceFrame;
    private PolygonView polygonView;
    private View view;
    private ProgressDialogFragment progressDialogFragment;
    private IScanner scanner;
    private Bitmap original;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof IScanner)) {
            throw new ClassCastException("Activity must implement IScanner");
        }
        this.scanner = (IScanner) activity;
    }


    //프래그먼트 초기화 작업
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.scan_fragment_layout, null);

        //초기화 메소드
        init();
        return view;
    }

    public ScanFragment() {

    }


    //초기화 : 뷰를 세팅하고, 비트맵을 이미지뷰에 세팅
    private void init() {
        sourceImageView = view.findViewById(R.id.sourceImageView);
        scanButton = view.findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new ScanButtonClickListener());
        sourceFrame = view.findViewById(R.id.sourceFrame);
        polygonView = view.findViewById(R.id.polygonView);

        sourceFrame.post(new Runnable() {
            @Override
            public void run() {

                // 원본 이미지의 비트맵을 가져온다.
                original = getBitmap();

                //원본 비트맵을 가져오는데 성공했다면
                if (original != null) {

                    //비트맵을 이미지뷰에 세팅한다.
                    setBitmap(original);
                }
            }
        });
    }

    //번들로 받았던 uri 로부터 비트맵을 가져오는 메소드
    private Bitmap getBitmap() {

        //번들로 받은 uri 를 받는다.
        Uri uri = getUri();

        try {

            //비트맵을 uri 에서 가져온다.
            Bitmap bitmap = Utils.getBitmap(getActivity(), uri);
            getActivity().getContentResolver().delete(uri, null, null);


            //비트맵 반환
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Uri getUri() {
        Uri uri = getArguments().getParcelable(ScanConstants.SELECTED_BITMAP);
        return uri;
    }


    private String getRealPath() {
        String realPath = getArguments().getString("imgPath");
        return realPath;
    }


    //가져온 비트맵을 이미지뷰에 세팅하고 폴리곤 뷰를 만든다.
    // 폴리곤 뷰는, 이미지뷰의 꼭지점을 지정하여 변형을 수행할 기준점을 만든다.

    private void setBitmap(Bitmap originalBitmap) {

        //프레임에 맞게 비트맵 크기를 변경
        Bitmap scaledBitmap = scaledBitmap(originalBitmap, sourceFrame.getWidth(), sourceFrame.getHeight());

        String imgPath = getRealPath();
        // 이미지를 상황에 맞게 회전시킨다
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imgPath);

        } catch (IOException e) {
            e.printStackTrace();
        }

        int exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);
        scaledBitmap = rotate(scaledBitmap, exifDegree);

        original = scaledBitmap;

        sourceImageView.setImageBitmap(original);

        //비트맵을 이미지뷰에 세팅
//        Glide.with(this).load(scaledBitmap).into(sourceImageView);


        //폴리곤뷰 생성을 위한 임시 비트맵 생성
//        Bitmap tempBitmap = ((BitmapDrawable) sourceImageView.getDrawable()).getBitmap();

        Bitmap tempBitmap = original;

        //임시 비트맵의 네 개의 꼭지점을 가져온다.
        Map<Integer, PointF> pointFs = getEdgePoints(tempBitmap);

        //폴리곤 뷰를 세팅 -> 꼭지점이 이미지뷰에 세팅된 것처럼 보이게 됨


        polygonView.setPoints(pointFs);
        polygonView.setVisibility(View.VISIBLE);

        //폴리곤 뷰의 패딩, 그래비티 등 설정
        int padding = (int) getResources().getDimension(R.dimen.scanPadding);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);
        layoutParams.gravity = Gravity.CENTER;

        polygonView.setLayoutParams(layoutParams);
    }


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


    //비트맵의 꼭지점을 가져오는 메소드
    private Map<Integer, PointF> getEdgePoints(Bitmap tempBitmap) {
        List<PointF> pointFs = getContourEdgePoints(tempBitmap);
        return orderedValidEdgePoints(tempBitmap, pointFs);
    }

    //꼭지점 8개 점을 가져오는 메소드 - Native Method 인 getPoints 사용
    private List<PointF> getContourEdgePoints(Bitmap tempBitmap) {

        //getPoints 네이티브 메소드를 사용, 비트맵의 8개 꼭지점을 가져온다.
        float[] points = ((ScanActivity) getActivity()).getPoints(tempBitmap);
        float x1 = points[0];
        float x2 = points[1];
        float x3 = points[2];
        float x4 = points[3];

        float y1 = points[4];
        float y2 = points[5];
        float y3 = points[6];
        float y4 = points[7];

        //꼭지점의 어레이 리스트를 만들고, 꼭지점들을 추가한다.
        List<PointF> pointFs = new ArrayList<>();
        pointFs.add(new PointF(x1, y1));
        pointFs.add(new PointF(x2, y2));
        pointFs.add(new PointF(x3, y3));
        pointFs.add(new PointF(x4, y4));
        return pointFs;
    }

    //비트맵의 끝자락을 꼭지점으로 지정하도록 해주는 메소드
    private Map<Integer, PointF> getOutlinePoints(Bitmap tempBitmap) {
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth(), 0));
        outlinePoints.put(2, new PointF(0, tempBitmap.getHeight()));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));
        return outlinePoints;
    }


    // 비트맵의 꼭지점을 반환하는 메소드인데, 현재 꼭지점 인식이 원활하지 않아서 무조건 이미지 끝자락을 반환하게 함
    private Map<Integer, PointF> orderedValidEdgePoints(Bitmap tempBitmap, List<PointF> pointFs) {


        Map<Integer, PointF> orderedPoints = polygonView.getOrderedPoints(pointFs);

        //무조건 이미지 끝자락을 반환하도록 한다.
        orderedPoints = getOutlinePoints(tempBitmap);
//        if (!polygonView.isValidShape(orderedPoints)) {
//            orderedPoints = getOutlinePoints(tempBitmap);
//        }
        return orderedPoints;
    }


    // 이미지의 꼭지점을 정하고 스캔 버튼을 클릭하면, 이미지의 변형이 수행된다.


    private class ScanButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            //폴리곤뷰의 꼭지점을 받는다.
            Map<Integer, PointF> points = polygonView.getPoints();
            if (isScanPointsValid(points)) {

                //스캔을 위한 ScanAsyncTask 를 실행
                new ScanAsyncTask(points).execute();
            } else {
                showErrorDialog();
            }
        }
    }

    private void showErrorDialog() {
        SingleButtonDialogFragment fragment = new SingleButtonDialogFragment(R.string.ok, getString(R.string.cantCrop), "Error", true);
        FragmentManager fm = getActivity().getFragmentManager();
        fragment.show(fm, SingleButtonDialogFragment.class.toString());
    }

    private boolean isScanPointsValid(Map<Integer, PointF> points) {
        return points.size() == 4;
    }

    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
    }

    private Bitmap getScannedBitmap(Bitmap original, Map<Integer, PointF> points) {
        int width = original.getWidth();
        int height = original.getHeight();
        float xRatio = (float) original.getWidth() / sourceImageView.getWidth();
        float yRatio = (float) original.getHeight() / sourceImageView.getHeight();

        float x1 = (points.get(0).x) * xRatio;
        float x2 = (points.get(1).x) * xRatio;
        float x3 = (points.get(2).x) * xRatio;
        float x4 = (points.get(3).x) * xRatio;
        float y1 = (points.get(0).y) * yRatio;
        float y2 = (points.get(1).y) * yRatio;
        float y3 = (points.get(2).y) * yRatio;
        float y4 = (points.get(3).y) * yRatio;
        Log.d("", "POints(" + x1 + "," + y1 + ")(" + x2 + "," + y2 + ")(" + x3 + "," + y3 + ")(" + x4 + "," + y4 + ")");
        Bitmap _bitmap = ((ScanActivity) getActivity()).getScannedBitmap(original, x1, y1, x2, y2, x3, y3, x4, y4);
        return _bitmap;
    }


    //스캔을 수행하기 위한 스레드
    private class ScanAsyncTask extends AsyncTask<Void, Void, Bitmap> {

        private Map<Integer, PointF> points;

        public ScanAsyncTask(Map<Integer, PointF> points) {
            this.points = points;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(getString(R.string.scanning));
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            //스캔된 비트맵을 가져오는 메소드 : native method 호출
            Bitmap bitmap = getScannedBitmap(original, points);
            Uri uri = Utils.getUri(getActivity(), bitmap);

            //scanActivity 에서 onScanFinish 수행
            scanner.onScanFinish(uri);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            bitmap.recycle();
            dismissDialog();
        }
    }

    // 스캔 진행중임을 표시하는 메소드
    protected void showProgressDialog(String message) {
        progressDialogFragment = new ProgressDialogFragment(message);
        FragmentManager fm = getFragmentManager();
        progressDialogFragment.show(fm, ProgressDialogFragment.class.toString());
    }

    protected void dismissDialog() {
        progressDialogFragment.dismissAllowingStateLoss();
    }

}