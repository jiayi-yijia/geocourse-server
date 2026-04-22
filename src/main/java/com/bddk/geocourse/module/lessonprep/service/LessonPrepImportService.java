package com.bddk.geocourse.module.lessonprep.service;

import com.bddk.geocourse.module.lessonprep.model.LessonPrepAttachmentImportRequest;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepAttachmentImportView;

public interface LessonPrepImportService {

    LessonPrepAttachmentImportView importFromAttachment(LessonPrepAttachmentImportRequest request);
}
