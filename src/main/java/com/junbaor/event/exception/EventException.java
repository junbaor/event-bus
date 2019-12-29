package com.junbaor.event.exception;

abstract public class EventException extends RuntimeException {
    private static final long serialVersionUID = -7402036348887345236L;

    public EventException() {
    }

    public EventException(String message) {
        super(message);
    }

    public EventException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventException(Throwable cause) {
        super(cause);
    }
}
