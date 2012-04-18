package controllers;

import models.chat.ChatRoom;
import play.Logger;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.With;

//@As(binder = ChatMessageBinder.class)

@With(Secure.class)
public class Chat extends Controller {
    public static void postMessage(@Required String messageText, @Required long chatRoomId) {
        final ChatRoom chatRoom = ChatRoom.<ChatRoom>findById(chatRoomId);
        validation.required("chatroom.dontExists", chatRoom);
        if (validation.hasErrors()) {
            notFound();
            Logger.warn("Chat.postMessage " + validation.toString());
        } else {
            chatRoom.appendMessageFrom(messageText, Security.connected());
            chatRoom.save();
        }
    }

    public static void getMessages(long chatRoomId) {
        ChatRoom chatRoom = ChatRoom.findById(chatRoomId);
        validation.required("chatroom.dontExists", chatRoom);
        if (validation.hasErrors()) {
            notFound();
            Logger.warn("Chat.getMessages " + validation.toString());
        } else {
            renderJSON(chatRoom.getMessages());
        }
    }
}

