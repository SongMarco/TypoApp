package nova.typoapp.notificationlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/*
알림 페이지를 위한 클래스

알림 아이템과 리스트가 정의돼있다.


 */


/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class NoticeContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<NoticeItem> ITEMS = new ArrayList<NoticeItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, NoticeItem> ITEM_MAP = new HashMap<String, NoticeItem>();

    private static void addItem(NoticeItem item) {
        ITEMS.add(item);

    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class NoticeItem {

        // 알림의 기본 내용

        int noticeID;

        int feedID;


        int commentID;

        String ownerEmail;


        public String noticeContent;

        public String noticeDate;

        // 알림을 클릭하면 갈 액티비티에 대한 문자열
        public String toActivity;

        public String profileUrl;


        // 알림을 클릭할 때 어디로 가야할 지 정의할 요소들 필요
        // 액티비티, 인텐트에 들어가야할 변수들. 추후 세팅할 것


        public NoticeItem(int noticeID, String ownerEmail, String noticeContent, String noticeDate, String toActivity, String profileUrl) {
            this.noticeID = noticeID;
            this.ownerEmail = ownerEmail;
            this.noticeContent = noticeContent;
            this.noticeDate = noticeDate;
            this.toActivity = toActivity;
            this.profileUrl = profileUrl;
        }

        public NoticeItem(int noticeID, int feedID, int commentID, String ownerEmail, String noticeContent, String noticeDate, String toActivity, String profileUrl) {
            this.noticeID = noticeID;
            this.feedID = feedID;
            this.commentID = commentID;
            this.ownerEmail = ownerEmail;
            this.noticeContent = noticeContent;
            this.noticeDate = noticeDate;
            this.toActivity = toActivity;
            this.profileUrl = profileUrl;
        }


    }
}
