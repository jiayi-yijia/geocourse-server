package com.bddk.geocourse.module.classroom.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("school_class_teacher")
public class SchoolClassTeacherDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long classId;
    private Long teacherId;
    private String roleCode;
    private Boolean isPrimary;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
