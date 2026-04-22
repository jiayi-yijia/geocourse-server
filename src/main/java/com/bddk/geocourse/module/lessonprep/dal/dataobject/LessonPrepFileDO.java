package com.bddk.geocourse.module.lessonprep.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lesson_prep_file")
public class LessonPrepFileDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private Long uploaderId;
    private String fileName;
    private String fileExt;
    private Long fileSize;
    private String contentType;
    private String accessUrl;
    private String storagePath;
    private String storageType;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
