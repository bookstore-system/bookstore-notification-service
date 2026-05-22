package com.notfound.bookstorenotificationservice.exception;

public class MissingCurrentUserException extends RuntimeException {

    public MissingCurrentUserException(String message) {
        super(message);
    }
}

