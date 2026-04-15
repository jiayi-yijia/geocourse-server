package com.bddk.geocourse.module.identity.stp;

import cn.dev33.satoken.stp.StpLogic;

/**
 * 学校管理员端 Sa-Token 工具类。
 */
public final class StpSchoolAdminUtil {

    public static final String LOGIN_TYPE = "school-admin";

    public static final StpLogic stpLogic = new StpLogic(LOGIN_TYPE);

    private StpSchoolAdminUtil() {
    }

}
