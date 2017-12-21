package nova.typoapp.subcoment;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class SubCommentContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<SubCommentItem> ITEMS = new ArrayList<SubCommentItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
//    public static final Map<String, SubCommentItem> ITEM_MAP = new HashMap<String, SubCommentItem>();



    public static void addItem(SubCommentItem item) {
        ITEMS.add(item);
//        ITEM_MAP.put(item.id, item);
    }

    public static void clearList() {
        ITEMS.clear();
//        ITEM_MAP.put(item.id, item);
    }


    private static SubCommentItem createDummyItem(int position) {
        return new SubCommentItem(String.valueOf(position), "Item " + position, makeDetails(position));
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
    public static class SubCommentItem {
        public  String id;
        public  String content;
        public  String details;

        public String subCommentimgProfileUrl;

        public String subCommentWriter;

        public String subCommentWriterEmail;


        public String subCommentDate;
        public String subCommentContent;

        public int commentID;
        public int subCommentID;
        int depth;

        public SubCommentItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        public SubCommentItem(int commentID, int subCommentID, int depth, String commentWriter, String commentContent, String commentDate , String imgProfileUrl) {
            this.commentID = commentID;
            this.subCommentID = subCommentID;
            this.depth = depth;

            this.subCommentWriter = commentWriter;
            this. subCommentContent =commentContent;
            this. subCommentDate = commentDate;

            this.subCommentimgProfileUrl = imgProfileUrl;
        }


        public SubCommentItem(int commentID, int subCommentID, int depth, String commentWriter, String subCommentWriterEmail,  String commentContent, String commentDate , String imgProfileUrl) {
            this.commentID = commentID;
            this.subCommentID = subCommentID;
            this.depth = depth;

            this.subCommentWriter = commentWriter;
            this.subCommentWriterEmail = subCommentWriterEmail;

            this. subCommentContent =commentContent;
            this. subCommentDate = commentDate;

            this.subCommentimgProfileUrl = imgProfileUrl;
        }









        @Override
        public String toString() {
            return content;
        }
    }
}
