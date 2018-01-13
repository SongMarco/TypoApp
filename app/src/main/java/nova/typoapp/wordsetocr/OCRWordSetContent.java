package nova.typoapp.wordsetocr;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018-01-13.
 */

public class OCRWordSetContent {


    public static ArrayList<WordItem> ITEMS = new ArrayList<>();




    private static void addItem(WordItem item) {
        ITEMS.add(item);
    }


    // 문자인식 단어장에 세팅될 아이템 객체
    public static class WordItem implements Parcelable {


        //아이템의 id, db에 없는 단어는 -1
        int idWord;

        //영어단어 스펠링
        String nameWord;

        //단어 뜻
        String meanWord;

        //단어 이미지 url
        String UrlWordImg;

        //Ocr 에서 단어장 이미지를 로컬로 세팅했을 때의 이미지 경로.
        String imgPath;

        //아이템의 deep copy 를 위한 생성자. - WordPuzzleActivity 서 사용됨
        public WordItem(WordItem item) {

            this.nameWord = item.nameWord;
            this.meanWord = item.meanWord;
            this.UrlWordImg = item.UrlWordImg;
            this.idWord = item.idWord;
        }


        public WordItem(int idWord, String urlWordImg, String nameWord, String meanWord) {
            this.idWord = idWord;
            this.UrlWordImg = urlWordImg;
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

        public void getItemInfo() {

            Log.e("getItemInfo", "name Word : " + nameWord + " // meanWord : " + meanWord + "// imgUrl : " + UrlWordImg);

        }


    }

}
