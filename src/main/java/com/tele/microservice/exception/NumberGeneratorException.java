package com.tele.microservice.exception;

public class NumberGeneratorException extends Exception {
    public NumberGeneratorException() {
    }

    public NumberGeneratorException(String message) {
        super(message);
    }

    public NumberGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public NumberGeneratorException(Throwable cause) {
        super(cause);
    }
}
