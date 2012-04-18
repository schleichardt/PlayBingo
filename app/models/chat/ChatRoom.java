package models.chat;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ChatRoom extends Model {

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn
    private List<ChatMessage> messages = new ArrayList<ChatMessage>();

    public int getMessageCount() {
        return messages.size();
    }

    public ChatMessage getLastMessage() {
        return messages.get(getMessageCount() - 1);
    }

    public void appendMessageFrom(String messageText, String author) {
        final ChatMessage chatMessage = new ChatMessage(messageText, author);
        messages.add(chatMessage);
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }
}
