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

        int idGroup;

        String nameGroup;

        String contentGroup;


        String emailGroupOwner;
        String nameGroupOwner;
        String UrlGroupImg;


        int numGroupMembers;

        String dateGroupMade;


        public GroupItem(int idGroup, String nameGroup, String contentGroup, String emailGroupOwner, String nameGroupOwner, String urlGroupImg, int numGroupMembers, String dateGroupMade) {
            this.idGroup = idGroup;
            this.nameGroup = nameGroup;
            this.contentGroup = contentGroup;
            this.emailGroupOwner = emailGroupOwner;
            this.nameGroupOwner = nameGroupOwner;
            UrlGroupImg = urlGroupImg;
            this.numGroupMembers = numGroupMembers;
            this.dateGroupMade = dateGroupMade;
        }

    }
}