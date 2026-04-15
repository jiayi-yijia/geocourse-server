package com.bddk.geocourse.framework.common.error;

public class ServiceException extends RuntimeException {

    private final int code;

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.message());
        this.code = errorCode.code();
    }

    public ServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.code();
    }

    public int getCode() {
        return code;
    }

}

