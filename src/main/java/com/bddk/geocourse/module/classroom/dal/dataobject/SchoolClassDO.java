package com.bddk.geocourse.module.classroom.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("school_class")
public class SchoolClassDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long schoolId;
    private String classCode;
    private String className;
    private String stageCode;
    private String gradeCode;
    private Long homeroomTeacherId;
    private Integer headcount;
    private Integer enrollmentYear;
    private String status;
    private Long resourceSpaceId;
    private String remark;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
