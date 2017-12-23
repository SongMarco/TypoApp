package nova.typoapp.newsfeed;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class SearchFeedContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<FeedItem> ITEMS_SEARCH = new ArrayList<FeedItem>();



    public static boolean called = false;

    public static void addItem(FeedItem item) {
        ITEMS_SEARCH.add(item);
//        ITEM_MAP.put(item.title, item);
    }

    //이곳에서 본격적으로 아이템의 뷰를 세팅한다.
    //여기서 아이템을 본격적으로 만들게 된다 @@@ 메소드를 고치던가 하자.

    /**
     * A dummy item representing a piece of content.
     */
    public static class FeedItem {
        public String writer;

        String writerEmail;

        public final String title;
        public final String content;

        public String imgUrl;

        public String imgProfileUrl;

        public String writtenDate;

        public int feedID;

        public int getCommentNum() {
            return commentNum;
        }

        public int commentNum;

        public int likeFeedNum;



        public String isLiked;
        public String details = "aa";


        public FeedItem(int feedID, int likeFeedNum, String isLiked  ,String writer, String title, String content, String imgUrl, String imgProfileUrl, String writtenDate, int commentNum, String writerEmail) {
            this.feedID = feedID;

            this.likeFeedNum = likeFeedNum;

            this.isLiked = isLiked;


            this.writer = writer;
            this.title = title;
            this.content = content;
            this.imgUrl = imgUrl;
            this.imgProfileUrl = imgProfileUrl;
            this.writtenDate = writtenDate;
            this.commentNum = commentNum;
            this.writerEmail = writerEmail;
        }




        @Override
        public String toString() {
            return content;
        }


        public String getInfo() {


            return this.feedID + this.writer + " " + this.title + " " + this.content;
        }

        public int getFeedID() {
            return feedID;
        }


        public String getImgUrl() {
            return imgUrl;
        }

        public void setImgUrl(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getWrittenDate() {
            return writtenDate;
        }

    }
}