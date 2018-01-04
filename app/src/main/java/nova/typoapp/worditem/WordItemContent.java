package nova.typoapp.worditem;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class WordItemContent {


    public static ArrayList<WordItem> ITEMS = new ArrayList<>();



    private static void addItem(WordItem item) {
        ITEMS.add(item);
    }


    public static class WordItem implements Parcelable {

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


        public WordItem(Parcel in) {
            idWord = in.readInt();
            UrlWordImg = in.readString();
            nameWord = in.readString();
            meanWord = in.readString();
        }

        public static final Creator<WordItem> CREATOR = new Creator<WordItem>() {
            @Override
            public WordItem createFromParcel(Parcel in) {
                return new WordItem(in);
            }

            @Override
            public WordItem[] newArray(int size) {
                return new WordItem[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(idWord);
            dest.writeString(UrlWordImg);
            dest.writeString(nameWord);
            dest.writeString(meanWord);
        }
    }
}
