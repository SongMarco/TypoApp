package nova.typoapp.wordset;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nova.typoapp.MainActivity;
import nova.typoapp.R;

public class AddWordSetOCRActivity extends AppCompatActivity {

    //구글 비전 api 가동에 필요한 키. 안드로이드 키로 제한하면 작동되지 않음 - 구글 클라우드 api 는 안드로이드용 api 가 아니기 때문.
    private static final String CLOUD_VISION_API_KEY = "AIzaSyAdZz-QCgViUa7XDCOOfeOO9bG6XviFPcs";
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";


    //문자 인식을 적용할 이미지뷰
    @BindView(R.id.imgAddWordSetCam)
    ImageView imgAddWordSetCam;


    @BindView(R.id.tvOcrContent)
    TextView tvOcrContent;
    ProgressDialog progressDialog;

    Uri imgUri;
    String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word_set_ocr);
        ButterKnife.bind(this);

        final Context context = this;
        imagePath = getIntent().getStringExtra("imagePath");

        imgUri = Uri.parse(getIntent().getStringExtra("imgUri"));


        Glide.with(this)
                .load(imgUri)

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


                        progressDialog = new ProgressDialog(AddWordSetOCRActivity.this);
                        progressDialog.setMessage("문자를 인식하는 중입니다...");
                        progressDialog.show();

                        uploadImage(imgUri);

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


        @Override
        protected void onPreExecute() {


        }

        @Override
        protected String doInBackground(Integer... integers) {

            // 이미지 디코딩을 위한 초기화.
            // 문자 인식 메소드를 이용하려면 이미지의 비트맵이 필요하다. - 먼저 이미지의 비트맵을 가져온다.
            image = ((BitmapDrawable) imgAddWordSetCam.getDrawable()).getBitmap();



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
        try {
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


    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                1200);

                callCloudVision(bitmap);

            } catch (IOException e) {
//                Log.d(TAG, "Image picking failed because " + e.getMessage());
//                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }


    @SuppressLint("StaticFieldLeak")
    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
//        mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("TEXT_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
                tvOcrContent.setText(result);
                progressDialog.dismiss();
            }
        }.execute();
    }


    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "문자 인식 결과:\n\n";

        List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
        if (labels != null) {
            message += labels.get(0).getDescription();
        } else {
            message += "nothing";
        }
        return message;

    }



}



