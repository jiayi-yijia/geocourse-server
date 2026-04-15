package com.bddk.geocourse.module.assignment.service;

import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.assignment.model.TeacherExamRankingView;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordQuery;
import com.bddk.geocourse.module.assignment.model.TeacherExamRecordView;
import com.bddk.geocourse.module.assignment.model.TeacherPaperQuery;
import com.bddk.geocourse.module.assignment.model.TeacherPaperSaveRequest;
import com.bddk.geocourse.module.assignment.model.TeacherPaperView;

import java.util.List;

public interface TeacherAssignmentService {

    PageResult<TeacherPaperView> pagePapers(TeacherPaperQuery query);

    TeacherPaperView getPaper(Long paperId);

    TeacherPaperView createPaper(TeacherPaperSaveRequest request);

    TeacherPaperView updatePaper(Long paperId, TeacherPaperSaveRequest request);

    void updatePaperStatus(Long paperId, String status);

    void deletePaper(Long paperId);

    PageResult<TeacherExamRecordView> pageExamRecords(TeacherExamRecordQuery query);

    void deleteExamRecord(Long recordId);

    List<TeacherExamRankingView> listRanking(Long paperId, Integer limit);
}
