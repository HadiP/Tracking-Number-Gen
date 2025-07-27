package com.tele.microservice.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IllegalInputFormatException extends IllegalArgumentException {

    public IllegalInputFormatException(String s) {
        super(s);
    }

    public IllegalInputFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalInputFormatException(Throwable cause) {
        super(cause);
    }
}
