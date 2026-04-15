package com.bddk.geocourse.module.identity.stp;

import cn.dev33.satoken.stp.StpLogic;

/**
 * 管理员端 Sa-Token 工具类。
 * 通过独立 loginType=admin 与未来的教师端、学生端会话隔离。
 */
public final class StpAdminUtil {

    /**
     * 管理员端登录体系标识。
     */
    public static final String LOGIN_TYPE = "admin";

    /**
     * 管理员端专用的 Sa-Token 逻辑实例。
     */
    public static final StpLogic stpLogic = new StpLogic(LOGIN_TYPE);

    private StpAdminUtil() {
    }

}
