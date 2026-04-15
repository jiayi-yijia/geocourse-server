package com.bddk.geocourse.module.assignment.service;

import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.assignment.model.TeacherExamGradeRequest;
import com.bddk.geocourse.module.assignment.model.TeacherExamGradingDetailView;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordQuery;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordView;

public interface TeacherExamGradingService {

    PageResult<TeacherExamRecordView> pageRecordsForGrading(TeacherExamRecordQuery query);

    TeacherExamGradingDetailView getGradingDetail(Long recordId);

    TeacherExamGradingDetailView gradeRecord(Long recordId, TeacherExamGradeRequest request);
}
