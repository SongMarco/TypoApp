package nova.typoapp.groupChat.ottoEventBus;

import nova.typoapp.groupChat.ChatTextContent.ChatItem;

/**
 * Created by Administrator on 2018-02-23.
 */

// otto 이벤트 버스를 통해 전달될 채팅 수신 이벤트. 채팅방의 id를 가지고 있다.
public class ChatRcvEvent {

    public ChatItem chatItem;

    public ChatRcvEvent(ChatItem chatItem) {
        this.chatItem = chatItem;
    }
}
