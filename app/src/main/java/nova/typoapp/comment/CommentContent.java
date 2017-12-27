package nova.typoapp.comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 *
 */
public class CommentContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<CommentItem> ITEMS = new ArrayList<CommentItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, CommentItem> ITEM_MAP = new HashMap<String, CommentItem>();

//    private static final int COUNT = 25;
//
//    static {
//        // Add some sample items.
//        for (int i = 1; i <= COUNT; i++) {
//            addItem(createCommentItem(i));
//        }
//    }

    public static void addItem(CommentItem item) {
        ITEMS.add(item);

//        ITEM_MAP.put(item.id, item);
    }

    public static void clearList() {
       ITEMS.clear();
//        ITEM_MAP.put(item.id, item);
    }

    private static CommentItem createCommentItem(int position) {
        return new CommentItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }
    public static CommentItem createCommentItem2(int commentID, String commentWriter, String commentContent, String commentDate) {
        return new CommentItem( commentID,  commentWriter,  commentContent,  commentDate);
    }
//    public static CommentItem createCommentItem3(int int commentID, String mCommentWriterView, String commentContent, String commentDate, int depth, String profileUrl) {
//        return new CommentItem( commentID,  mCommentWriterView,  commentContent,  commentDate);
//    }

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
    public static class CommentItem {
        public  String id;
        public  String content;
        public  String details;

        public String imgProfileUrl;

        public String commentWriter;

        public String commentWriterEmail;


        public String commentDate;
        public String commentContent;

        public int commentID;
        public int feedID;

        int depth;

        int subCommentNum;


        public CommentItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        public CommentItem(int commentID, String commentWriter, String commentContent, String commentDate){
            this.commentID = commentID;

            this.commentWriter = commentWriter;
            this. commentContent = commentContent;
            this. commentDate =commentDate;

        }


        public CommentItem(int commentID, int feedID, int depth, String commentWriter, String commentContent, String commentDate, String imgProfileUrl){

            this.commentID = commentID;
            this.feedID = feedID;

            this.depth = depth;

            this.commentWriter = commentWriter;
            this. commentContent = commentContent;
            this. commentDate =commentDate;
            this.imgProfileUrl = imgProfileUrl;

        }

        public CommentItem(int commentID, int feedID, int depth, int subCommentNum , String commentWriter, String commentContent, String commentDate, String imgProfileUrl){

            this.commentID = commentID;
            this.feedID = feedID;

            this.depth = depth;

            this.subCommentNum = subCommentNum;

            this.commentWriter = commentWriter;
            this. commentContent = commentContent;
            this. commentDate =commentDate;
            this.imgProfileUrl = imgProfileUrl;

        }

        public CommentItem(int commentID, int feedID, int depth, int subCommentNum , String commentWriter, String commentWriterEmail, String commentContent, String commentDate, String imgProfileUrl){

            this.commentID = commentID;
            this.feedID = feedID;

            this.depth = depth;

            this.subCommentNum = subCommentNum;

            this.commentWriter = commentWriter;
            this.commentWriterEmail = commentWriterEmail;

            this. commentContent = commentContent;
            this. commentDate =commentDate;
            this.imgProfileUrl = imgProfileUrl;

        }

        @Override
        public String toString() {
            return content;
        }
    }
}
