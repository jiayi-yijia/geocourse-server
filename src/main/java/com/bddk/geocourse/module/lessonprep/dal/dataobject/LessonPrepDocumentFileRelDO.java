package com.bddk.geocourse.module.lessonprep.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lesson_prep_document_file_rel")
public class LessonPrepDocumentFileRelDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long documentId;
    private Long fileId;
    private String relationType;
    private Integer sortNo;
    private LocalDateTime linkedAt;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
