package com.sample.system.ssm.service.application.handler;

import com.sample.system.ssm.service.domain.exception.SsmDomainException;
import com.sample.system.ssm.service.domain.ports.input.service.StatusService;
import com.sample.system.ssm.service.domain.response.base.BaseResponse;
import com.sample.system.ssm.service.domain.response.base.ErrorDetail;
import com.sample.system.ssm.service.domain.utility.StringUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestControllerAdvice
@RequiredArgsConstructor
public class SsmGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final StatusService statusService;

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> handleAnyException(Exception exception, WebRequest request) {
        ExceptionUtils.getStackTrace(exception);
        log.error("exception in request ==> {} with message {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getStackTrace());
        log.error("error in request" + exception.getMessage());
        try {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(String.valueOf(StatusService.GENERAL_ERROR));
            errorDetail.setMessage(statusService.findByCode(String.valueOf(StatusService.GENERAL_ERROR)).getPersianDescription());
            return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(false, errorDetail));
        } catch (Exception ex) {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(String.valueOf(StatusService.GENERAL_ERROR));
            errorDetail.setMessage("Exception: error read description");
            return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(false, errorDetail));
        }
    }

    @ExceptionHandler(value = {AuthenticationException.class})
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> handleAuthenticationException(AuthenticationException exception, WebRequest request) {
        log.error("AuthenticationException in request ==> {} , message ===> {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getMessage());
        try {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(String.valueOf(StatusService.SESSION_NOT_VALID));
            errorDetail.setMessage(statusService.findByCode(String.valueOf(StatusService.SESSION_NOT_VALID)).getPersianDescription());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new BaseResponse<>(false, errorDetail));
        } catch (Exception ex) {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(String.valueOf(StatusService.SESSION_NOT_VALID));
            errorDetail.setMessage("AuthenticationException: error read description");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new BaseResponse<>(false, errorDetail));
        }

    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> handleAccessDeniedException(AccessDeniedException exception, WebRequest request) {
        log.error("AccessDeniedException in request ==> {} message ===> {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getMessage());
        try {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(String.valueOf(StatusService.USER_HAS_NOT_PERMISSION));
            errorDetail.setMessage(statusService.findByCode(String.valueOf(StatusService.USER_HAS_NOT_PERMISSION)).getPersianDescription());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new BaseResponse<>(false, errorDetail));
        } catch (Exception ex) {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(String.valueOf(StatusService.USER_HAS_NOT_PERMISSION));
            errorDetail.setMessage("AccessDeniedException: error read description");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new BaseResponse<>(false, errorDetail));
        }
    }

    @ExceptionHandler(value = {SsmDomainException.class})
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> handleServiceException(SsmDomainException exception, WebRequest request) {
        log.error("service exception in request ==> {} , message ===> {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getMessage());
        try {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(String.valueOf(exception.getStatus()));
            String message = statusService.findByCode(String.valueOf(exception.getStatus())).getPersianDescription();
            if (StringUtils.isPersianString(exception.getMessage())) {
                message = StringUtils.fixSomeWord(exception.getMessage());
            }
            if (exception.getParameters() != null) {
                message = StringSubstitutor.replace(message, exception.getParameters(), "${", "}");
            }
            errorDetail.setMessage(message);
            return ResponseEntity.status(exception.getHttpStatus()).body(new BaseResponse<>(false, errorDetail));
        } catch (Exception ex) {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(String.valueOf(exception.getStatus()));
            errorDetail.setMessage("InternalServiceException: error read description");
            return ResponseEntity.status(exception.getHttpStatus()).body(new BaseResponse<>(false, errorDetail));
        }
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> handleConstraintViolationExceptions(ConstraintViolationException exception, WebRequest request) {
        log.error("exception in request ==> {} , message ===> {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getMessage());
        ConstraintViolation<?> violation = exception.getConstraintViolations().iterator().next();
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setCode(String.valueOf(StatusService.INPUT_PARAMETER_NOT_VALID));
        errorDetail.setMessage(violation.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(false, errorDetail));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.debug("SsmGlobalExceptionHandler.handleMethodArgumentNotValid started");
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName;
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = null;

            if (error instanceof FieldError fieldError) {
                fieldName = fieldError.getField();
                rejectedValue = fieldError.getRejectedValue();
                log.error("error in parameter ({}) , and error is ({}) and input value is ({})", fieldName, errorMessage, rejectedValue);
            } else {
                fieldName = error.getObjectName();
                log.error("error in object ({}) , and error is ({})", fieldName, errorMessage);
            }
            errors.put(fieldName, errorMessage);
        });

        // Get first error message (from field errors if available, otherwise from all errors)
        String firstErrorMessage;
        if (!ex.getBindingResult().getFieldErrors().isEmpty()) {
            firstErrorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        } else if (!ex.getBindingResult().getAllErrors().isEmpty()) {
            firstErrorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        } else {
            firstErrorMessage = "Validation failed";
        }

        ErrorDetail errorDetail = new ErrorDetail(firstErrorMessage, String.valueOf(StatusService.INPUT_PARAMETER_NOT_VALID));
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(false, errorDetail));
    }

}
