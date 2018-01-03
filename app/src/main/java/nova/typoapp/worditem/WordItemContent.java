package nova.typoapp.worditem;


import java.util.ArrayList;
import java.util.List;


public class WordItemContent {


    public static List<WordItem> ITEMS = new ArrayList<>();



    private static void addItem(WordItem item) {
        ITEMS.add(item);
    }


    public static class WordItem {

        int idWord;

        public String UrlWordImg;

        public String nameWord;
        public String meanWord;

        public WordItem(int idWord, String urlWordImg, String nameWord, String meanWord) {
            this.idWord = idWord;
            UrlWordImg = urlWordImg;
            this.nameWord = nameWord;
            this.meanWord = meanWord;
        }


    }
}
