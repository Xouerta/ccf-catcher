package com.ccf.sercurity.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class PlatformExceptionHandler {
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResult> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("NoResourceFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResult(404, e.getMessage()));
    }

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handlePlatformMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, Object> errors = new HashMap<>();
        StringBuffer buffer = new StringBuffer();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put("msg", fieldName + errorMessage);
            errors.put("code", 400);
            buffer.append(String.format("不合格字段 %s %s   ", fieldName, errorMessage));
        });

        log.warn(buffer.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResult> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResult(400, e.getMessage()));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handlePlatformHandlerMethodValidationException(HandlerMethodValidationException e) {
        Map<String, Object> errors = new HashMap<>();
        e.getAllErrors().forEach(error -> {
            errors.put("msg", error.getDefaultMessage());
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
