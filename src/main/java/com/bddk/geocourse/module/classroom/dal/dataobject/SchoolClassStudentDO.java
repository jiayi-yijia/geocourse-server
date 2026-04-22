package com.bddk.geocourse.module.classroom.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("school_class_student")
public class SchoolClassStudentDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long classId;
    private Long studentId;
    private String studentNo;
    private String joinStatus;
    private LocalDateTime joinedTime;
    private LocalDateTime leftTime;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
