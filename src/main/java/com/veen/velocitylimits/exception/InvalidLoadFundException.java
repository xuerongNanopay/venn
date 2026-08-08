package com.veen.velocitylimits.exception;

public class InvalidLoadFundException extends RuntimeException {
    
    public InvalidLoadFundException(String message) {
        super(message);
    }

    public InvalidLoadFundException(String message, Throwable cause) {
        super(message, cause);
    }
}
