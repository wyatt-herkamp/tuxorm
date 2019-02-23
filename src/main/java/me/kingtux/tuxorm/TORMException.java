package me.kingtux.tuxorm;

public class TORMException extends RuntimeException {
    public TORMException() {
    }

    public TORMException(String message) {
        super(message);
    }

    public TORMException(String message, Throwable cause) {
        super(message, cause);
    }

    public TORMException(Throwable cause) {
        super(cause);
    }

    public TORMException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
