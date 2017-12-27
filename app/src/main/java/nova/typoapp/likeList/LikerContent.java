package nova.typoapp.likeList;

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
public class LikerContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<LikerItem> ITEMS = new ArrayList<LikerItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, LikerItem> ITEM_MAP = new HashMap<String, LikerItem>();

    private static final int COUNT = 25;

//    static {
//        // Add some sample items.
//        for (int i = 1; i <= COUNT; i++) {
//            addItem(createDummyItem(i));
//        }
//    }

    public static void addItem(LikerItem item) {
        ITEMS.add(item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class LikerItem {
        public  String id;
        public  String content;


        public String likerProfileUrl;
        public String likerName;
        public String likerEmail;




//        public LikerItem(String id, String content, String details) {
//            this.id = id;
//            this.content = content;
//            this.details = details;
//        }

        public LikerItem(String likerName, String likerEmail, String likerProfileUrl) {
           this.likerProfileUrl = likerProfileUrl;
           this.likerName = likerName;
           this.likerEmail = likerEmail;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
