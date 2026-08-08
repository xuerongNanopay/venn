package com.veen.velocitylimits.exception;

public class CustomerEntityLockException extends RuntimeException {

    public CustomerEntityLockException(String customerId) {
        super("Unable to create or lock customer: " + customerId);
    }
}
