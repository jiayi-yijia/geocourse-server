package com.bddk.geocourse.module.assignment.service;

import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.assignment.model.TeacherExamPublishQuery;
import com.bddk.geocourse.module.assignment.model.TeacherExamPublishSaveRequest;
import com.bddk.geocourse.module.assignment.model.TeacherExamPublishView;

public interface TeacherExamPublishService {

    PageResult<TeacherExamPublishView> pagePublishes(TeacherExamPublishQuery query);

    TeacherExamPublishView getPublish(Long publishId);

    TeacherExamPublishView createPublish(TeacherExamPublishSaveRequest request);

    TeacherExamPublishView updatePublish(Long publishId, TeacherExamPublishSaveRequest request);

    void updatePublishStatus(Long publishId, String status);

    void deletePublish(Long publishId);
}
