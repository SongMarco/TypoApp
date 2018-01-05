package nova.typoapp.worditem;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

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

        //아이템의 deep copy 를 위한 생성자. - WordPuzzleActivity 서 사용됨
        public WordItem(WordItem item){

            this.nameWord = item.nameWord;
            this.meanWord = item.meanWord;
            this.UrlWordImg = item.UrlWordImg;
            this.idWord = item.idWord;
        }


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

        public void getItemInfo(){

            Log.e("getItemInfo", "name Word : "+nameWord+" // meanWord : "+meanWord+ "// imgUrl : "+UrlWordImg);

        }



    }
}
