package nova.typoapp.wordset;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.R;

public class AddWordSetOCRActivity extends AppCompatActivity {


    //문자 인식을 적용할 이미지뷰
    @BindView(R.id.imgAddWordSetCam)
    ImageView imgAddWordSetCam;


    String imagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word_set_ocr);
        ButterKnife.bind(this);

        final Context context = this;
        imagePath = getIntent().getStringExtra("imagePath");

        Glide.with(this)
                .load(imagePath)

//               글라이드에서 이미지 로딩이 완료될 경우 발생하는 이벤트.
                .listener(new RequestListener<Drawable>() {

                    //문자 이미지 로딩 실패, 에러 메시지 출력
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(context, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    //문자 이미지 로딩 성공 -> 문자 인식 기능 수행
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                        OcrTask ocrTask = new OcrTask();
                        ocrTask.execute();

                        return false;
                    }
                })
                .into(imgAddWordSetCam);


    }




    Bitmap image; // 문자 인식을 위한 비트맵 이미지
    String datapath; // 언어 트레이닝 데이터 경로
    private TessBaseAPI mTess; //Tess API reference

    //서버에서 단어장을 추가하도록 하는 태스크
    public class OcrTask extends AsyncTask<Integer, String, String> {



        ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());

        @Override
        protected void onPreExecute() {


//            progressDialog.show();


        }

        @Override
        protected String doInBackground(Integer... integers) {

            // 이미지 디코딩을 위한 초기화.
            // 문자 인식 메소드를 이용하려면 이미지의 비트맵이 필요하다. - 먼저 이미지의 비트맵을 가져온다.
            image = ((BitmapDrawable)imgAddWordSetCam.getDrawable()).getBitmap();


            //언어 트레이닝 데이터의 경로를 가져온다.
            datapath = getFilesDir()+ "/tesseract/";

            //트레이닝데이터가 카피되어 있는지 체크
            checkFile(new File(datapath + "tessdata/"));

            //Tesseract API 언어 설정. eng: 영어// kor :한글
            String lang = "eng";

            mTess = new TessBaseAPI();
            mTess.init(datapath, lang);

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    String OCRresult = null;
                    mTess.setImage(image);
                    OCRresult = mTess.getUTF8Text();
                    TextView OCRTextView = (TextView) findViewById(R.id.tvOcrContent);
                    OCRTextView.setText(OCRresult);

                }
            });

            return null;
        }

        @Override

        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            progressDialog.dismiss();

        }

    }



    //언어 트레이닝 데이터를 복사한다.
    private void copyFiles() {
        try{
            //파일의 경로를 세팅
            String filepath = datapath + "/tessdata/eng.traineddata";

            // Assets 디렉토리에 파일을 넣기 위해 AssetManager 세팅
            AssetManager assetManager = getAssets();

            // 트레이닝 데이터의 본래 파일을 인풋스트림에서 가져온다.
            InputStream instream = assetManager.open("tessdata/eng.traineddata");

            // 복사할 파일의 경로로 아웃풋 스트림을 내보낸다.
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;

            // 인풋스트림 -> 아웃풋 스트림으로 파일 복사 수행
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }

            // 복사 완료시 아웃풋 스트림을 flush (출력) 하여 파일 복사 완료
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //문자 인식 트레이닝 데이터가 세팅된 지 확인한다.
    private void checkFile(File dir) {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if(!dir.exists()&& dir.mkdirs()) {
            copyFiles();
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
    }

}



