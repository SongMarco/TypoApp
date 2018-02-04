package nova.typoapp.groupMember;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-01-29.
 */

public class GroupMemberContent {

        /**
         * An array of sample (dummy) items.
         */
        public static List<MemberItem> ITEMS = new ArrayList<>();

        /**
         * A map of sample (dummy) items, by ID.
         */



        /**
         * A dummy item representing a piece of content.
         */

        /*
        멤버 아이템 클래스

        그룹에서 멤버 정보를 가져올 때 사용하는 아이템 클래스다.

         */
        public static class MemberItem {


            int idGroup;  // 멤버가 속한 그룹의 id

            String nameMember;

            String emailMember;

            String imgUrlMEmber;

            public MemberItem(int idGroup, String nameMember, String emailMember, String imgUrlMEmber) {
                this.idGroup = idGroup;
                this.nameMember = nameMember;
                this.emailMember = emailMember;
                this.imgUrlMEmber = imgUrlMEmber;
            }

        }

}
