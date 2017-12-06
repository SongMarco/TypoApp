package nova.typoapp.newsfeed;

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
    public static List<FeedItem> ITEMS = new ArrayList<FeedItem>();

    public static boolean called = false;
    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, FeedItem> ITEM_MAP = new HashMap<String, FeedItem>();

    private static final int COUNT = 25;





    public static void addItem(FeedItem item) {
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
    public static FeedItem createFeed3(String writer, String title, String content) {
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



        public String getInfo(){



            return this.writer+" "+this.title+" "+this.content;
        }

    }
}
