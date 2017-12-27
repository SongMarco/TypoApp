package nova.typoapp;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;

import nova.typoapp.retrofit.AddCookiesInterceptor;
import nova.typoapp.retrofit.ApiService;
import nova.typoapp.retrofit.ReceivedCookiesInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;

import static nova.typoapp.retrofit.ApiService.API_URL;

/**
 * Created by Administrator on 2017-12-28.
 */

/*
어싱크 태스크를 개발하면서 자주 사용하게 됐다.

단순한 태스크를 미리 만들어두고 필요할 때 가져다 쓸 필요가 생겼다.

이에 본 태스크를 만들어둔다.
 */
public class aaStandardAsyncTask extends AsyncTask<Integer, String, String> {

    String json_result;
    private Context mContext;

    // context를 가져오는 생성자. 이를 통해 메인 액티비티의 함수에 접근할 수 있다.

    public aaStandardAsyncTask(Context context) {
        mContext = context;
    }

    @Override
    protected String doInBackground(Integer... integers) {

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


        //좋아요를 한 대상의 타입이다. 여기서는 게시물이므로 feed 라 하였다.
        String type = "feed";

        // 태스크를 만들 때 파라미터로 전송한 feed ID 값이다.
        int feed_ID = integers[0];

        // 레트로핏 콜 객체를 만든다. 파라미터로 게시물의 ID값, 게시물의 타입을 전송한다.
        //todo 좋아요를 자꾸 하면서 장난을 치면 알림을 막는 로직이 필요하다.
        Call<ResponseBody> comment = apiService.likeFeed(feed_ID, type );

        try {

            json_result = comment.execute().body().string();
            return json_result;
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override

    protected void onPostExecute(String result) {

        super.onPostExecute(result);

        //게시물에 좋아요를 적용/취소하였다.

    }

}