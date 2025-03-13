package com.groceryapp.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponseWrapper<T> {
    private boolean success;
    private T data;

    public static <T> GenericResponseWrapper<T> success(T data) {
        return GenericResponseWrapper.<T> builder().success(true).data(data).build();
    }

    public static <T> GenericResponseWrapper<T> success() {
        return GenericResponseWrapper.<T> builder().success(true).build();
    }
}
