package nova.typoapp.wordset;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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

    @BindView(R.id.btnMakeWordSet)
    Button btnMakeWordSet;


    ProgressDialog progressDialog;

    Uri imgUri;
    String imagePath;

    Bitmap image; // 문자 인식을 위한 비트맵 이미지

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


    String wordSet; // 인식한 문자의 스트링
    String[] arrWord;

    ArrayList<String> listWord;
    HashSet<String> distinctData;

    //인식한 문자는 텍스트뷰에 들어있다. 이를 스트링으로 전환
    @OnClick(R.id.btnMakeWordSet)
    public void clickMakeWord() {


        //텍스트뷰의 내용을 스트링으로 전환
        wordSet = tvOcrContent.getText().toString();

        //정규식 패턴 정의 - 알파벳과 ' 으로 이루어진 단어 (ex) didn't, abstract 등
        Pattern pattern = Pattern.compile("[a-zA-Z']+");

        //알파벳을 가진 단어만 추출
        Matcher matcher = pattern.matcher(wordSet);

        listWord = new ArrayList<>();

        //matcher -> list 변환. 영어로만 이루어진 세트 추출 완료.
        while (matcher.find()) {
            listWord.add(matcher.group());
        }

        //이제 쓸모없는 단어들을 제외해야 한다.
        // 리스트를 돌며 특정 조건 하에 있는 스트링을 삭제하는 코드
        for (Iterator<String> iter = listWord.iterator(); iter.hasNext(); ) {

            // 먼저 스트링 요소를 꺼내본다.
            String strElement = iter.next();


            //꺼낸 스트링 요소가 단어장에 부적합하다면
            if (!isWord(strElement)) {

                //스트링 요소를 삭제한다.
                iter.remove();
            }
        }


        //대문자 소문자가 섞인 단어들을 변환
        for (int i = 0; i < listWord.size(); i++) {
            //리스트의 스트링을 가져와 소문자로 변환
            String lowerCase = listWord.get(i).toLowerCase();

            //소문자 변환한 단어를 원래 단어와 교체
            listWord.set(i, lowerCase);
        }


        //중복된 단어를 제거해야 한다.

        //임시 어레이리스트를 하나 더 만들고, 아이템을 중복되지 않게 추가해나간다.

        //임시 어레이리스트
        ArrayList<String> tempList = new ArrayList<>();

        //원본 리스트의 크기만큼 반복
        for (int i = 0; i < listWord.size(); i++) {

            //tempList에 본래 리스트에서 가져온 단어가 포함되지 않았다면 추가한다. -> 중복 제거됨
            if(!tempList.contains(listWord.get(i) )  )
                tempList.add(listWord.get(i));
        }

        //tempList로 복사. -> 문장에서 단어의 순서를 유지하면서 단어가 중복되지 않게 만들어졌음
        listWord = new ArrayList<>(tempList);



        for (int i = 0; i < listWord.size(); i++) {

            Log.e(TAG, "clickMakeWord: " + listWord.get(i));
        }

        //해쉬셋 확인


    }


    // 단어장에 들어갈 단어인지 아닌지를 판별하는 메소드
    // 들어갈 수 있으면 true, 들어갈 수 없으면 false 반환 - a, the, to 등
    public boolean isWord(String word) {


        //먼저 두 글자 이하의 단어를 제거한다.
        if (word.length() <= 2) {

            return false;
        }

        //세 글자 이상인 단어 중 쓸모없는 단어
        Set<String> setNotWord = new HashSet<String>();
        setNotWord.add("the");
        setNotWord.add("didn't");
        setNotWord.add("don't");
        setNotWord.add("can't");
        setNotWord.add("for");
        setNotWord.add("was");
        setNotWord.add("their");
        setNotWord.add("our");
        setNotWord.add("and");

        //해쉬 셋 안의 금지된 단어와 비교, 같은 케이스가 나오면 false 반환
        for (String badWord : setNotWord) {

            // equalsIgnoreCase 를 이용, 스트링 값을 대소문자 구분없이 비교.
            if (word.equalsIgnoreCase(badWord) || word.contains("'")) return false;

        }

        //1234
        //false 조건을 모두 거쳐와도 문제가 없다면 true 반환.
        return true;
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
        String message = "";

        List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
        if (labels != null) {
            message += labels.get(0).getDescription();
        } else {
            message += "nothing";
        }
        return message;

    }


}



