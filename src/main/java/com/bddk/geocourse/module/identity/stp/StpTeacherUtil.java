package com.bddk.geocourse.module.identity.stp;

import cn.dev33.satoken.stp.StpLogic;

/**
 * 教师端 Sa-Token 工具类。
 * 通过独立 loginType=teacher 与平台后台、学校后台会话隔离。
 */
public final class StpTeacherUtil {

    /**
     * 教师端登录体系标识。
     */
    public static final String LOGIN_TYPE = "teacher";

    /**
     * 教师端专用的 Sa-Token 逻辑实例。
     */
    public static final StpLogic stpLogic = new StpLogic(LOGIN_TYPE);

    private StpTeacherUtil() {
    }

}
