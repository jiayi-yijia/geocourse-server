package com.bddk.geocourse.module.lessonprep.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lesson_prep_document")
public class LessonPrepDocumentDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long teacherId;
    private Long courseId;
    private String courseName;
    private String title;
    private String docType;
    private String status;
    private String summary;
    private String contentType;
    private String contentText;
    private String sourceType;
    private Long sourceDocumentId;
    private LocalDateTime publishedAt;
    private LocalDateTime lastEditedAt;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
