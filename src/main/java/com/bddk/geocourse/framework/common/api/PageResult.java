package com.bddk.geocourse.framework.common.api;

import java.io.Serializable;
import java.util.List;

public record PageResult<T>(List<T> list, long total, long pageNo, long pageSize) implements Serializable {

    public static <T> PageResult<T> of(List<T> list, long total, long pageNo, long pageSize) {
        return new PageResult<>(list, total, pageNo, pageSize);
    }
}
