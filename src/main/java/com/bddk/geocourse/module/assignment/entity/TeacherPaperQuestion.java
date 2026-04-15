package com.bddk.geocourse.module.assignment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("assignment_paper_question")
public class TeacherPaperQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long paperId;
    private Long questionId;
    private BigDecimal score;
    private Integer sortNo;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
