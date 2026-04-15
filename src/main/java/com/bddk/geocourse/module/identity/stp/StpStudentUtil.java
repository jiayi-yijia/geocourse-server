package com.bddk.geocourse.module.identity.stp;

import cn.dev33.satoken.stp.StpLogic;

/**
 * 学生端 Sa-Token 工具类。
 * 通过独立 loginType=student 与后台、教师端会话隔离。
 */
public final class StpStudentUtil {

    /**
     * 学生端登录体系标识。
     */
    public static final String LOGIN_TYPE = "student";

    /**
     * 学生端专用的 Sa-Token 逻辑实例。
     */
    public static final StpLogic stpLogic = new StpLogic(LOGIN_TYPE);

    private StpStudentUtil() {
    }

}
