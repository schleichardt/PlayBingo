package exceptions;

public class BingoException extends Exception {
    public BingoException() {
    }

    public BingoException(String s) {
        super(s);
    }

    public BingoException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public BingoException(Throwable throwable) {
        super(throwable);
    }
}
