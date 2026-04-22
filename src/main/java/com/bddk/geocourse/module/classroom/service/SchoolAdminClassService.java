package com.bddk.geocourse.module.classroom.service;

import com.bddk.geocourse.framework.common.api.PageResult;
import com.bddk.geocourse.module.classroom.model.SchoolAdminClassQuery;
import com.bddk.geocourse.module.classroom.model.SchoolAdminClassStudentQuery;
import com.bddk.geocourse.module.classroom.model.SchoolAdminClassViews;
import org.springframework.web.multipart.MultipartFile;

public interface SchoolAdminClassService {

    PageResult<SchoolAdminClassViews.Item> pageClasses(SchoolAdminClassQuery query);

    SchoolAdminClassViews.Detail getClassDetail(Long classId);

    PageResult<SchoolAdminClassViews.Student> pageStudents(Long classId, SchoolAdminClassStudentQuery query);

    SchoolAdminClassViews.StudentImportResult importStudents(Long classId, MultipartFile file);

    byte[] downloadStudentImportTemplate();
}
