package com.veen.velocitylimits.exception;

public class InvalidLoadRecordException extends RuntimeException {
    
    public InvalidLoadRecordException(String message) {
        super(message);
    }

    public InvalidLoadRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
