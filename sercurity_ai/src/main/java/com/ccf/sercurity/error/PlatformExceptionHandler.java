package com.ccf.sercurity.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class PlatformExceptionHandler {
    @ExceptionHandler(PlatformException.class)
    public ResponseEntity<ErrorResult> handlePlatformException(PlatformException e) {
        log.warn("PlatformException: {}", e.getMsg());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResult(e.getCode(), e.getMsg()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResult> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResult(400, e.getParameterName() + "字段错误"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResult> handleConstraintViolationException(ConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResult(400, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleException(Exception e) {
        log.warn("Exception: {}", e.getMessage());
        // TODO RELEASE要删掉e.printStackTrace()
        e.printStackTrace();
        return ResponseEntity.status(500).body(new ErrorResult(500, "其他异常"));
    }
}
