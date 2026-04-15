package com.bddk.geocourse.module.assignment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("assignment_exam_publish")
public class TeacherExamPublish {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long paperId;
    private String title;
    private String description;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal passScore;
    private BigDecimal totalScore;
    private Integer questionCount;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
