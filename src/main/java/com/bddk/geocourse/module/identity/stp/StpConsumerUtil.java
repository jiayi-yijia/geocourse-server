package com.bddk.geocourse.module.identity.stp;

import cn.dev33.satoken.stp.StpLogic;

/**
 * 普通用户端 Sa-Token 工具类。
 * 通过独立 loginType=consumer 与后台、教师端、学生端会话隔离。
 */
public final class StpConsumerUtil {

    /**
     * 普通用户端登录体系标识。
     */
    public static final String LOGIN_TYPE = "consumer";

    /**
     * 普通用户端专用的 Sa-Token 逻辑实例。
     */
    public static final StpLogic stpLogic = new StpLogic(LOGIN_TYPE);

    private StpConsumerUtil() {
    }

}
