package nova.typoapp.wordset;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class WordSetContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<WordSetItem> ITEMS = new ArrayList<WordSetItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */


    private static void addItem(WordSetItem item) {
        ITEMS.add(item);

    }


    /**
     * A dummy item representing a piece of content.
     */

     public static class WordSetItem {
        public  String id;
        public  String content;
        public  String details;

        public String titleWordSet;
        public int numWords;
        public String nameWordSetOwner;

        String UrlOwnerProfileImg;


        public WordSetItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        public WordSetItem(String titleWordSet, int numWords, String nameWordSetOwner) {

            this.titleWordSet = titleWordSet;
            this.numWords = numWords;
            this.nameWordSetOwner = nameWordSetOwner;
        }

        public WordSetItem(String titleWordSet, int numWords, String nameWordSetOwner, String urlOwnerProfileImg) {
            this.titleWordSet = titleWordSet;
            this.numWords = numWords;
            this.nameWordSetOwner = nameWordSetOwner;
            UrlOwnerProfileImg = urlOwnerProfileImg;
        }

    }
}
