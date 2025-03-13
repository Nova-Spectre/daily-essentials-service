package com.groceryapp.dto.response.common;

import com.groceryapp.constant.common.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseWrapper {
    private boolean success;
    private String message;
    private Integer errorCode;
    private List<String> errors;

    public ErrorResponseWrapper(String message) {
    }

    public static ErrorResponseWrapper fromErrorCode(ErrorCode errorCode) {
        return ErrorResponseWrapper.builder().success(false).message(errorCode.getMessage())
                .errorCode(errorCode.getCode()).build();
    }

    public static ErrorResponseWrapper fromErrorCode(ErrorCode errorCode, List<String> errors) {
        return ErrorResponseWrapper.builder().success(false).message(errorCode.getMessage())
                .errorCode(errorCode.getCode()).errors(errors).build();
    }
}
