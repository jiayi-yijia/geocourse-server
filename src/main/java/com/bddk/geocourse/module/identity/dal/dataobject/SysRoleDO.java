package com.bddk.geocourse.module.identity.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色表。
 */
@Data
@TableName("sys_role")
public class SysRoleDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String roleCode;
    private String roleName;
    private String roleType;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;

}
