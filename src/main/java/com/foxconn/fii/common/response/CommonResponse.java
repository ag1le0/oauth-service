package com.foxconn.fii.common.response;

import lombok.Value;
import org.springframework.http.HttpStatus;

@Value(staticConstructor = "of")
public class CommonResponse<T> {

    private HttpStatus status;

    private ResponseCode code;

    private String message;

    private T result;

    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.of(HttpStatus.OK, ResponseCode.SUCCESS, "success", data);
    }
}
