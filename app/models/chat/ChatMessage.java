package models.chat;


import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
public class ChatMessage extends Model {

    public static enum Type {TEXT_MESSAGE, LEAVE_MESSAGE}

    ;

    @Enumerated(EnumType.STRING)
    private Type type = Type.TEXT_MESSAGE;
    private String messageText;
    private String author;

    public ChatMessage() {
    }

    public ChatMessage(String messageText) {
        this.messageText = messageText;
        type = Type.TEXT_MESSAGE;
    }

    public ChatMessage(String messageText, String author) {
        this(messageText);
        setAuthor(author);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public static ChatMessage leaveMessage() {
        final ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Type.LEAVE_MESSAGE);
        return chatMessage;
    }

    public String getText() {
        return messageText;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }
}
