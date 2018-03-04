package nova.typoapp.group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018-01-27.
 */

public class GroupContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<GroupItem> ITEMS = new ArrayList<>();


    public static class GroupItem {

        public int idGroup;

        public String nameGroup;

        public String contentGroup;


        String emailGroupOwner;
        String nameGroupOwner;
        public String UrlGroupImg;


        public int numGroupMembers;

        String dateGroupMade;

        public boolean isMemberGroup; // 그룹에 확인했는지를 결정하는 변수. true -> 가입됨

        public GroupItem(int idGroup, String nameGroup, String contentGroup, String emailGroupOwner, String nameGroupOwner, String urlGroupImg, int numGroupMembers, String dateGroupMade, boolean isMemberGroup) {
            this.idGroup = idGroup;
            this.nameGroup = nameGroup;
            this.contentGroup = contentGroup;
            this.emailGroupOwner = emailGroupOwner;
            this.nameGroupOwner = nameGroupOwner;
            UrlGroupImg = urlGroupImg;
            this.numGroupMembers = numGroupMembers;
            this.dateGroupMade = dateGroupMade;
            this.isMemberGroup = isMemberGroup;
        }

    }
}