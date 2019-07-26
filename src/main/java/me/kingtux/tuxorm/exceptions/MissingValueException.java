package me.kingtux.tuxorm.exceptions;

public class MissingValueException extends RuntimeException {
    public MissingValueException(String s) {
        super(s);
    }
}
