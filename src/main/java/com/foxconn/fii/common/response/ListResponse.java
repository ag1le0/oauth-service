package com.foxconn.fii.common.response;

import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.util.List;

@Value(staticConstructor = "of")
public class ListResponse<T> {

    private HttpStatus status;

    private ResponseCode code;

    private String message;

    private List<T> data;

    private int size;

    public static <T> ListResponse<T> of(HttpStatus status, ResponseCode code, String message, List<T> data) {
        return ListResponse.of(status, code, message, data, data.size());
    }

    public static <T> ListResponse<T> success(List<T> data) {
        return ListResponse.of(HttpStatus.OK, ResponseCode.SUCCESS, "success", data, data.size());
    }

    public static <T> ListResponse<T> success(List<T> data, int size) {
        return ListResponse.of(HttpStatus.OK, ResponseCode.SUCCESS, "success", data, size);
    }

    public static <T> ListResponse<T> success(Page<T> data) {
        return ListResponse.of(HttpStatus.OK, ResponseCode.SUCCESS, "success", data.getContent(), (int) data.getTotalElements());
    }
}
