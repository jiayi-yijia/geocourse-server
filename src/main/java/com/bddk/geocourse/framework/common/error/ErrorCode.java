package com.bddk.geocourse.framework.common.error;

public record ErrorCode(int code, String message) {

    public static final ErrorCode BAD_REQUEST = new ErrorCode(40000, "请求参数错误");
    public static final ErrorCode UNAUTHORIZED = new ErrorCode(40100, "未登录或登录已过期");
    public static final ErrorCode AUTH_LOGIN_FAILED = new ErrorCode(40101, "管理员账号或密码错误");
    public static final ErrorCode FORBIDDEN = new ErrorCode(40300, "无权访问当前资源");
    public static final ErrorCode NOT_FOUND = new ErrorCode(40400, "资源不存在");
    public static final ErrorCode TENANT_REQUIRED = new ErrorCode(41001, "缺少租户标识");
    public static final ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(50000, "服务内部异常");

}

