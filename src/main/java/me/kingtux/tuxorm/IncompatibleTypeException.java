package me.kingtux.tuxorm;

public class IncompatibleTypeException extends RuntimeException {
    public IncompatibleTypeException(String s) {
        super(s);
    }

    public IncompatibleTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompatibleTypeException(Throwable cause) {
        super(cause);
    }

    public IncompatibleTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IncompatibleTypeException() {
    }
}
