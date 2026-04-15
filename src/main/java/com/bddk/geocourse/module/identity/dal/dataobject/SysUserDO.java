package com.bddk.geocourse.module.identity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表。
 */
@Data
@TableName("sys_user")
public class SysUserDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String userNo;
    private String username;
    private String nickname;
    private String realName;
    private String passwordHash;
    private String salt;
    private String phone;
    private String email;
    private String avatarUrl;
    private Integer gender;
    private String userType;
    private String registerSource;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;

}
