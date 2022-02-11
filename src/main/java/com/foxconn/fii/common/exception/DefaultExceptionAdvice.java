package com.foxconn.fii.common.exception;

import com.foxconn.fii.common.response.CommonResponse;
import com.foxconn.fii.common.response.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@ControllerAdvice
public class DefaultExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<CommonResponse<Boolean>> handleCommonException(CommonException ce) {
        CommonResponse<Boolean> response = CommonResponse.of(HttpStatus.BAD_REQUEST, ResponseCode.FAILED, ce.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CommonResponse<Boolean>> handleBadCredentialsException(BadCredentialsException ce) {
        CommonResponse<Boolean> response = CommonResponse.of(HttpStatus.UNAUTHORIZED, ResponseCode.FAILED, ce.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Boolean>> handleException(Exception e) throws IOException {
        log.error("### unhandled exception ", e);
        CommonResponse<Boolean> response = CommonResponse.of(HttpStatus.BAD_REQUEST, ResponseCode.FAILED, "Exception: " + e.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}