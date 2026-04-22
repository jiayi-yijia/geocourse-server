package com.bddk.geocourse.module.course.service;

import com.bddk.geocourse.module.course.model.CourseResourcePreviewView;
import org.springframework.core.io.Resource;

public interface CourseResourcePreviewService {

    CourseResourcePreviewView getPreview(Long resourceId);

    Resource getInlineContent(Long resourceId);

    Resource getSlideImage(Long resourceId, Integer pageNo);
}
