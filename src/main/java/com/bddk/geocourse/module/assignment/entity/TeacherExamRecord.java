package com.bddk.geocourse.module.assignment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("assignment_exam_record")
public class TeacherExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long publishId;
    private String publishTitle;
    private Long paperId;
    private Long studentId;
    private String studentName;
    private BigDecimal score;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private String answers;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Integer windowSwitches;
    private Long graderId;
    private LocalDateTime gradedTime;
    private String reviewComment;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
