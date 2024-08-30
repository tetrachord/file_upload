package com.example.exception;

public class IpAddressBlockedException extends RuntimeException {

    public IpAddressBlockedException(String message) {
        super(message);
    }
}
