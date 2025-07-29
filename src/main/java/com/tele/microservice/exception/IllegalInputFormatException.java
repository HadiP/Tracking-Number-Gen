package com.tele.microservice.exception;

import lombok.Getter;

@Getter
public class IllegalInputFormatException extends IllegalArgumentException {

    private final String field;

    public IllegalInputFormatException(String field, String message) {
        super(message);
        this.field = field;
    }

    public IllegalInputFormatException(String field, String message, Throwable cause) {
        super(message, cause);
        this.field = field;
    }

    public IllegalInputFormatException(String field, Throwable cause) {
        super(cause);
        this.field = field;
    }

}
