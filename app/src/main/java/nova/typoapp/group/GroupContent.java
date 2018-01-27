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

    /**
     * A map of sample (dummy) items, by ID.
     */


    private static void addItem(GroupItem item) {
        ITEMS.add(item);
    }


    /**
     * A dummy item representing a piece of content.
     */

    public static class GroupItem {

        int idGroup;

        String nameGroup;


        String emailGroupOwner;
        String nameGroupOwner;
        String UrlOwnerProfileImg;


        int numGroupMembers;

        String dateGroupMade;


        public GroupItem(int idGroup, String nameGroup, String emailGroupOwner, String nameGroupOwner, String urlOwnerProfileImg, int numGroupMembers, String dateGroupMade) {
            this.idGroup = idGroup;
            this.nameGroup = nameGroup;
            this.emailGroupOwner = emailGroupOwner;
            this.nameGroupOwner = nameGroupOwner;
            UrlOwnerProfileImg = urlOwnerProfileImg;
            this.numGroupMembers = numGroupMembers;
            this.dateGroupMade = dateGroupMade;
        }

    }
}