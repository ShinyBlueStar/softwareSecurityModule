package com.sample.system.ssm.service.domain.exception;

public class DomainException extends Exception {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
