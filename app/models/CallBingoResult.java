package models;

public class CallBingoResult {
    private final boolean hasWon;
    private final String messageKey;

    public CallBingoResult(boolean hasWon, String messageKey) {
        this.hasWon = hasWon;
        this.messageKey = messageKey;
    }

    public boolean isHasWon() {
        return hasWon;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
