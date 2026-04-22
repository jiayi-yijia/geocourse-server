package com.bddk.geocourse.module.lessonprep.service;

import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentDetailView;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentPageItemView;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentPageQuery;
import com.bddk.geocourse.module.lessonprep.model.LessonPrepDocumentSaveRequest;

import java.util.List;

public interface LessonPrepDocumentService {

    PageResult<LessonPrepDocumentPageItemView> pageDocuments(LessonPrepDocumentPageQuery query);

    LessonPrepDocumentDetailView getDocument(Long documentId);

    LessonPrepDocumentDetailView createDocument(LessonPrepDocumentSaveRequest request);

    LessonPrepDocumentDetailView updateDocument(Long documentId, LessonPrepDocumentSaveRequest request);

    void deleteDocument(Long documentId);

    void batchDeleteDocuments(List<Long> documentIds);

    LessonPrepDocumentDetailView copyDocument(Long documentId, String title);

    LessonPrepDocumentDetailView publishDocument(Long documentId);
}
