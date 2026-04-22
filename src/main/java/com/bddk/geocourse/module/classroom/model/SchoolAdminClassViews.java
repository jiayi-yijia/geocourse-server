package com.bddk.geocourse.module.classroom.model;

import java.time.LocalDateTime;
import java.util.List;

public final class SchoolAdminClassViews {

    private SchoolAdminClassViews() {
    }

    public record Item(
            Long id,
            String classCode,
            String className,
            String stageCode,
            String gradeCode,
            String status,
            Long homeroomTeacherId,
            String homeroomTeacherName,
            Long studentCount,
            Long courseCount,
            Long resourceSpaceId,
            LocalDateTime createdTime
    ) {
    }

    public record Teacher(
            Long teacherId,
            String teacherName,
            String username,
            String roleCode,
            boolean isPrimary
    ) {
    }

    public record Course(
            Long courseId,
            String courseCode,
            String courseName,
            String relationType
    ) {
    }

    public record Stats(
            long studentCount,
            long activeStudentCount,
            long leftStudentCount,
            long teacherCount,
            long courseCount,
            long resourceCount,
            long lessonCount,
            long homeworkCount,
            long examCount
    ) {
    }

    public record Detail(
            Long id,
            String classCode,
            String className,
            String stageCode,
            String gradeCode,
            Integer enrollmentYear,
            Integer headcount,
            String status,
            Long schoolId,
            Long homeroomTeacherId,
            String homeroomTeacherName,
            Long resourceSpaceId,
            String resourceSpacePath,
            String remark,
            LocalDateTime createdTime,
            LocalDateTime updatedTime,
            List<Teacher> teachers,
            List<Course> courses,
            Stats stats
    ) {
    }

    public record Student(
            Long studentId,
            String studentNo,
            String studentName,
            String username,
            String phone,
            String joinStatus,
            LocalDateTime joinedTime,
            LocalDateTime leftTime
    ) {
    }

    public record StudentImportFailure(
            int rowNo,
            Long userId,
            String studentNo,
            String username,
            String phone,
            String name,
            Long matchedStudentId,
            String matchedStudentName,
            String reason
    ) {
    }

    public record StudentImportResult(
            String fileName,
            int totalRows,
            int addedCount,
            int reactivatedCount,
            int ignoredCount,
            int successCount,
            int failureCount,
            List<StudentImportFailure> failures
    ) {
    }
}
