package com.bddk.geocourse.module.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("qb_question_choice")
public class QuestionChoice {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long questionId;
    private String choiceKey;
    private String choiceText;
    private Boolean correct;
    private Integer sortNo;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
