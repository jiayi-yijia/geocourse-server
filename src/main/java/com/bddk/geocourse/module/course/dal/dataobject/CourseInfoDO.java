package com.bddk.geocourse.module.course.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("course_info")
public class CourseInfoDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tenantId;
    private String courseCode;
    private String courseName;
    private String courseType;
    private String subjectCode;
    private String coverUrl;
    private String introText;
    private Long teacherId;
    private String saleType;
    private BigDecimal priceAmount;
    private String publishStatus;
    private String learnMode;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private Long updateBy;
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    private String remark;
}
