package com.junbaor.event.service;

public class GoblinServiceException extends RuntimeException {
    private static final long serialVersionUID = 402921636817030857L;

    public GoblinServiceException() {
    }

    public GoblinServiceException(String message) {
        super(message);
    }

    public GoblinServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoblinServiceException(Throwable cause) {
        super(cause);
    }
}
