package com.bddk.geocourse.module.assignment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("assignment_exam_answer")
public class TeacherExamAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long examRecordId;
    private Long questionId;
    private String questionType;
    private String questionTitle;
    private String standardAnswer;
    private String userAnswer;
    private BigDecimal maxScore;
    private BigDecimal score;
    private Integer correctFlag;
    private String aiComment;
    private String teacherComment;
    private Integer reviewed;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
