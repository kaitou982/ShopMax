package com.shop.customerservice.service;

public class AiServiceException extends RuntimeException {

    private final Integer code;

    public AiServiceException(String message) {
        super(message);
        this.code = 2001;
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
        this.code = 2001;
    }

    public Integer getCode() {
        return code;
    }
}
