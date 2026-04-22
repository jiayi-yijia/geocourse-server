package com.bddk.geocourse.module.lessonprep.service;

import com.bddk.geocourse.module.lessonprep.model.LessonPrepFileUploadView;
import org.springframework.web.multipart.MultipartFile;

public interface LessonPrepFileService {

    LessonPrepFileUploadView upload(MultipartFile file);
}
