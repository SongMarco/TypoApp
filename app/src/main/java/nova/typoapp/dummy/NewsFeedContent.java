package nova.typoapp.dummy;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class NewsFeedContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<FeedItem> ITEMS = new ArrayList<FeedItem>();

    public static boolean called = false;
    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, FeedItem> ITEM_MAP = new HashMap<String, FeedItem>();

    private static final int COUNT = 25;

    /////네트워킹 파트
    public static class taskCallFeeds extends AsyncTask<Void, Integer, Boolean>
    {
        String json_result = "";
        String passwordEnc = "";
        @Override
        protected Boolean doInBackground(Void... unused) {

            try {
/* 서버연결 */
                URL url = new URL(
                        "http://115.68.231.13/project/android/callAllFeeds.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

/* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
//                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

/* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;


                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                json_result = buff.toString().trim();

                //<editor-fold desc="json 파싱 관련파트.">
                // json_result는 결과값으로 가져온 json String이다. json오브젝트에 이 스트링을 담는다.


                JSONArray jsonRes = null;
                try {
                    jsonRes = new JSONArray(json_result);

                    for(int i=0; i < jsonRes.length(); i++){
                        JSONObject jObject = jsonRes.getJSONObject(i);  // JSONObject 추출
                        String writer = jObject.getString("writer");
                        String title = jObject.getString("title");
                        String content = jObject.getString("text_content");

                        FeedItem productFeed =   createFeed3(writer, title, content);
                        addItem(productFeed);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // RECV 데이터에 php에서 뱉은 echo가 들어간다!
                Log.e("RECV DATA", json_result);

                //json을 성공적으로 서버에서 수신했다. 쿠키를 저장시키자


                //</editor-fold>


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            called = true;
            return called;
        }

        //post에서 받아온 json을 아이템으로 전환
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);






        }
    }
//
//    static {
//        // Add some sample items.
//        for (int i = 1; i <= COUNT; i++) {
//            addItem(createFeed(i));
//        }
//    }


    private static void addItem(FeedItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.title, item);
    }


    //todo 어싱크로 제이슨 불러오고, 여기다 세팅하기
    //todo restful로 리팩토링하기

    //이곳에서 본격적으로 아이템의 뷰를 세팅한다.
    //여기서 아이템을 본격적으로 만들게 된다 @@@ 메소드를 고치던가 하자.
    private static FeedItem createFeed(int position) {
        return new FeedItem("작성자 : SongYC123", "제목"+ String.valueOf(position), "내용 " + position);
    }
    private static FeedItem createFeed3(String writer, String title, String content) {
        return new FeedItem(writer, title, content);
    }



    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class FeedItem {
        public String writer;
        public final String title;
        public final String content;
        public String details = "aa";

        public FeedItem(String writer, String title, String content) {
            this.writer = writer;
            this.title = title;
            this.content = content;
        }
        public FeedItem(String writer, String title, String content, String details) {
            this.writer = writer;
            this.title = title;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
