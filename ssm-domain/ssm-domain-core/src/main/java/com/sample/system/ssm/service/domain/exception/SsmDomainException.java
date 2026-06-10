package com.sample.system.ssm.service.domain.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import java.util.Map;

@Getter
@Setter
public class SsmDomainException extends DomainException {

    private int status;
    private HttpStatus httpStatus;
    private Map<String, String> parameters;

    public SsmDomainException(String message) {
        super(message);
    }

    public SsmDomainException(int status) {
        super("");
        this.status = status;
    }

    public SsmDomainException(String message, int status, HttpStatus httpStatus) {
        super(message);
        this.status = status;
        this.httpStatus = httpStatus;
    }

    public SsmDomainException(String message, int status, HttpStatus httpStatus, Map<String, String> parameters) {
        super(message);
        this.status = status;
        this.httpStatus = httpStatus;
        this.parameters = parameters;
    }


    public SsmDomainException(String message, Throwable cause, int status){
        super(message, cause);
        this.status = status;
    }


}
