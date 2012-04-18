package controllers;

import com.google.gson.Gson;
import models.chat.ChatMessage;
import models.chat.ChatRoom;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ChatTest extends BingoAppFunctionalTest {
    private ChatRoom chatRoom;

    @Before
    public void init() {
        super.login();
        chatRoom = new ChatRoom();
        chatRoom.save();
    }

    @Test
    public void postMessage() {
        final long initialNumberOfMessages = chatRoom.getMessageCount();
        final Map<String, String> parameters = new HashMap<String, String>();
        final String messageText = "Hallo, Welt";
        final ChatMessage chatMessage = new ChatMessage(messageText);
        parameters.put("messageText", chatMessage.getMessageText());
        parameters.put("chatRoomId", Long.toString(chatRoom.getId()));
        final String messageUrl = String.format("/chat/post/message/", parameters);
        assertIsOk(POST(messageUrl, parameters));
        chatRoom.refresh();
        assertEquals("one more message should exist", initialNumberOfMessages + 1, chatRoom.getMessageCount());
        assertEquals("last message is correct", messageText, chatRoom.getLastMessage().getText());
    }

    @Test
    public void getMessages() throws UnsupportedEncodingException {
        chatRoom.appendMessageFrom("Bla 1", "One");
        chatRoom.appendMessageFrom("Bla 2", "Two");
        chatRoom.appendMessageFrom("Bla 3", "Two");
        chatRoom.save();
        final String url = String.format("/chat/get/messages/%d", chatRoom.getId());
        final Http.Response response = GET(url);
        assertIsOk(response);
        final String content = response.out.toString(response.encoding);
        ChatMessage[] messages = new Gson().fromJson(content, ChatMessage[].class);
        assertEquals("number of messages", 3, messages.length);
        assertTrue(messages[1].getMessageText().equals("Bla 2"));
    }
}
