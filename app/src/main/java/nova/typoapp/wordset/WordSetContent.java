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


        public int idWordSet;

        public String nameWordSet;
        public int numWords;
        public String nameWordSetOwner;

        public String UrlOwnerProfileImg;


        public WordSetItem(int idWordSet, String nameWordSet, int numWords, String nameWordSetOwner, String urlOwnerProfileImg) {
            this.idWordSet = idWordSet;
            this.nameWordSet = nameWordSet;
            this.numWords = numWords;
            this.nameWordSetOwner = nameWordSetOwner;
            UrlOwnerProfileImg = urlOwnerProfileImg;
        }
    }
}
